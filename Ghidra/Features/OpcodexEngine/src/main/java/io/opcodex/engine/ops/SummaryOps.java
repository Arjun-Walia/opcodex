/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import com.google.gson.*;

import ghidra.program.model.address.*;
import ghidra.program.model.data.StringDataInstance;
import ghidra.program.model.listing.Data;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.*;
import ghidra.program.model.symbol.*;
import ghidra.program.util.DefinedStringIterator;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.spi.EngineServices;

/** The at-a-glance triage summary and raw memory reads for the hex view. */
final class SummaryOps {

	static final int MAX_READ = 64 * 1024;
	private static final int ENTROPY_SAMPLE = 1 << 20;
	private static final int MAX_STRINGS_SCANNED = 20_000;
	private static final int MAX_NOTABLE = 50;
	private static final int MAX_IMPORTS_PER_LIBRARY = 500;

	private static final Map<String, Pattern> STRING_KINDS = new LinkedHashMap<>();
	static {
		STRING_KINDS.put("url", Pattern.compile("(?i)\\b(?:https?|ftp|wss?)://[^\\s\"'<>]{3,}"));
		// Four octets of 0-255, not part of a longer dotted number or a quoted version attribute.
		STRING_KINDS.put("ip", Pattern.compile("(?<![\\w.=\"'])" +
			"(?:(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)" +
			"(?::\\d{1,5})?(?![\\w.])"));
		STRING_KINDS.put("email", Pattern.compile("\\b[\\w.+-]+@[\\w-]+\\.[\\w.-]{2,}\\b"));
		STRING_KINDS.put("registry",
			Pattern.compile("(?i)\\b(?:HKEY_[A-Z_]+|HKLM|HKCU|SOFTWARE\\\\[\\w\\\\ ]+)"));
		STRING_KINDS.put("path",
			Pattern.compile("(?i)(?:\\b[a-z]:\\\\[^\\s\"]+|(?:^|\\s)/(?:etc|usr|tmp|var|bin|home|proc|dev)/\\S*)"));
	}

	private static final Map<String, String> RISKY = loadRiskyApis();

	private SummaryOps() {
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject read = FunctionOps.programParam();
		Schemas.property(read, "address", Schemas.string("First address to read."), true);
		Schemas.property(read, "length",
			Schemas.integer("Bytes to read, up to 65536.", 1, Integer.MAX_VALUE), true);
		return List.of(
			new Operation("program.summary",
				"Summarises a program for triage: format, architecture, entry points, sections " +
					"with entropy, imports by library, risky API usage and notable strings.",
				OperationClass.READ, FunctionOps.programParam(), Schemas.any("Summary."),
				(ctx, params) -> summary(FunctionOps.program(services, params))),
			new Operation("memory.read",
				"Reads up to 64 KiB of initialized bytes, stopping at the end of a block.",
				OperationClass.READ, read, Schemas.any("Bytes as hex."),
				(ctx, params) -> read(FunctionOps.program(services, params), params)));
	}

	private static JsonObject summary(Program program) {
		JsonObject out = new JsonObject();
		out.addProperty("name", program.getName());
		out.addProperty("format", Objects.requireNonNullElse(program.getExecutableFormat(), ""));
		out.addProperty("language", program.getLanguageID().getIdAsString());
		out.addProperty("processor", program.getLanguage().getProcessor().toString());
		out.addProperty("compiler", program.getCompilerSpec().getCompilerSpecID().getIdAsString());
		out.addProperty("endian", program.getLanguage().isBigEndian() ? "big" : "little");
		out.addProperty("addressSize",
			program.getAddressFactory().getDefaultAddressSpace().getSize());
		out.addProperty("imageBase", program.getImageBase().toString());
		out.addProperty("functionCount", internalFunctionCount(program));
		out.add("blocks", blocks(program));
		out.add("entryPoints", entryPoints(program));
		JsonArray imports = new JsonArray();
		JsonArray risky = new JsonArray();
		imports(program, imports, risky);
		out.add("imports", imports);
		out.add("riskyApis", risky);
		strings(program, out);
		return out;
	}

	/** Functions defined in the program itself, without imported (external) functions. */
	static int internalFunctionCount(Program program) {
		int external = 0;
		var externals = program.getFunctionManager().getExternalFunctions();
		while (externals.hasNext()) {
			externals.next();
			external++;
		}
		return program.getFunctionManager().getFunctionCount() - external;
	}

	private static JsonArray blocks(Program program) {
		JsonArray out = new JsonArray();
		for (MemoryBlock block : program.getMemory().getBlocks()) {
			JsonObject b = new JsonObject();
			b.addProperty("name", block.getName());
			b.addProperty("start", block.getStart().toString());
			b.addProperty("end", block.getEnd().toString());
			b.addProperty("size", block.getSize());
			b.addProperty("permissions", (block.isRead() ? "r" : "-") +
				(block.isWrite() ? "w" : "-") + (block.isExecute() ? "x" : "-"));
			b.addProperty("initialized", block.isInitialized());
			if (block.isInitialized()) {
				b.addProperty("entropy", Math.round(entropy(block) * 1000) / 1000.0);
			}
			out.add(b);
		}
		return out;
	}

	/** Shannon entropy in bits per byte over up to 1 MiB of the block. */
	private static double entropy(MemoryBlock block) {
		long[] counts = new long[256];
		long total = 0;
		byte[] buffer = new byte[8192];
		long limit = Math.min(block.getSize(), ENTROPY_SAMPLE);
		try {
			while (total < limit) {
				int n = block.getBytes(block.getStart().add(total), buffer, 0,
					(int) Math.min(buffer.length, limit - total));
				if (n <= 0) {
					break;
				}
				for (int i = 0; i < n; i++) {
					counts[buffer[i] & 0xff]++;
				}
				total += n;
			}
		}
		catch (MemoryAccessException | AddressOutOfBoundsException e) {
			// Use what was read.
		}
		if (total == 0) {
			return 0;
		}
		double entropy = 0;
		for (long count : counts) {
			if (count > 0) {
				double p = (double) count / total;
				entropy -= p * (Math.log(p) / Math.log(2));
			}
		}
		return entropy;
	}

	private static JsonArray entryPoints(Program program) {
		JsonArray out = new JsonArray();
		SymbolTable symbols = program.getSymbolTable();
		AddressIterator entries = symbols.getExternalEntryPointIterator();
		while (entries.hasNext() && out.size() < 200) {
			Address address = entries.next();
			JsonObject e = new JsonObject();
			e.addProperty("address", address.toString());
			Symbol primary = symbols.getPrimarySymbol(address);
			e.addProperty("name", primary == null ? address.toString() : primary.getName(true));
			out.add(e);
		}
		return out;
	}

	private static void imports(Program program, JsonArray imports, JsonArray risky) {
		ExternalManager externals = program.getExternalManager();
		Set<String> riskySeen = new HashSet<>();
		for (String library : externals.getExternalLibraryNames()) {
			if (ghidra.program.model.listing.Library.UNKNOWN.equals(library)) {
				continue;
			}
			JsonArray functions = new JsonArray();
			ExternalLocationIterator locations = externals.getExternalLocations(library);
			while (locations.hasNext()) {
				String name = locations.next().getLabel();
				if (functions.size() < MAX_IMPORTS_PER_LIBRARY) {
					functions.add(name);
				}
				String category = RISKY.get(name);
				if (category != null && riskySeen.add(name)) {
					JsonObject r = new JsonObject();
					r.addProperty("name", name);
					r.addProperty("library", library);
					r.addProperty("category", category);
					risky.add(r);
				}
			}
			JsonObject lib = new JsonObject();
			lib.addProperty("library", library);
			lib.add("functions", functions);
			imports.add(lib);
		}
	}

	private static void strings(Program program, JsonObject out) {
		JsonArray notable = new JsonArray();
		int count = 0;
		for (Data data : DefinedStringIterator.forProgram(program)) {
			if (++count > MAX_STRINGS_SCANNED) {
				break;
			}
			if (notable.size() >= MAX_NOTABLE) {
				continue;
			}
			String value = StringDataInstance.getStringDataInstance(data).getStringValue();
			if (value == null) {
				continue;
			}
			for (Map.Entry<String, Pattern> kind : STRING_KINDS.entrySet()) {
				if (kind.getValue().matcher(value).find()) {
					JsonObject s = new JsonObject();
					s.addProperty("address", data.getAddress().toString());
					s.addProperty("value", value.length() > 300 ? value.substring(0, 300) : value);
					s.addProperty("kind", kind.getKey());
					notable.add(s);
					break;
				}
			}
		}
		out.addProperty("stringCount", Math.min(count, MAX_STRINGS_SCANNED));
		out.add("notableStrings", notable);
	}

	private static JsonObject read(Program program, JsonObject params) throws RpcException {
		Address start = Params.address(program, "address", Params.requireString(params, "address"));
		int length = Params.integer(params, "length", 256);
		if (length > MAX_READ) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, "length must be at most 65536.");
		}
		Memory memory = program.getMemory();
		MemoryBlock block = memory.getBlock(start);
		if (block == null || !block.isInitialized()) {
			throw new RpcException(ErrorCode.NOT_FOUND, "No initialized bytes at " + start);
		}
		long available = block.getEnd().subtract(start) + 1;
		byte[] bytes = new byte[(int) Math.min(length, available)];
		int n;
		try {
			n = memory.getBytes(start, bytes);
		}
		catch (MemoryAccessException e) {
			throw new RpcException(ErrorCode.NOT_FOUND, "Can't read bytes at " + start);
		}
		JsonObject out = new JsonObject();
		out.addProperty("address", start.toString());
		out.addProperty("length", n);
		out.addProperty("hex", HexFormat.of().formatHex(bytes, 0, n));
		out.addProperty("block", block.getName());
		return out;
	}

	private static Map<String, String> loadRiskyApis() {
		Map<String, String> out = new HashMap<>();
		try (InputStream in = SummaryOps.class.getResourceAsStream(
			"/io/opcodex/engine/risky-apis.json")) {
			if (in == null) {
				return out;
			}
			JsonObject rules = JsonParser.parseReader(
				new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			for (Map.Entry<String, JsonElement> category : rules.entrySet()) {
				for (JsonElement name : category.getValue().getAsJsonArray()) {
					out.put(name.getAsString(), category.getKey());
				}
			}
		}
		catch (IOException | RuntimeException e) {
			ghidra.util.Msg.warn(SummaryOps.class, "Risky API rules unavailable: " + e.getMessage());
		}
		return out;
	}
}
