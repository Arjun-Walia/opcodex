/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.gson.*;

import ghidra.program.model.address.Address;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.util.Msg;
import ghidra.util.exception.DuplicateNameException;
import ghidra.util.exception.InvalidInputException;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.journal.JournalEntry;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.session.CallContext;
import io.opcodex.engine.spi.EngineServices;
import io.opcodex.engine.workspace.Workspace;

/**
 * Renames and comments. Every change checks the caller's revision, records the value before and
 * after in the journal, and can later be reverted on its own.
 */
final class EditOps {

	static final String RENAME = "symbol.rename";
	static final String COMMENT = "comment.set";
	static final Set<String> REVERTIBLE = Set.of(RENAME, COMMENT);
	static final int MAX_COMMENT_CHARS = 64 * 1024;

	private EditOps() {
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject rename = FunctionOps.programParam();
		Schemas.property(rename, "address", Schemas.string("Address of the symbol."), true);
		Schemas.property(rename, "newName", Schemas.string("New name."), true);
		revisionParam(rename);

		JsonObject comment = FunctionOps.programParam();
		Schemas.property(comment, "address", Schemas.string("Address."), true);
		Schemas.property(comment, "kind",
			Schemas.enumOf("Comment kind.", "eol", "pre", "post", "plate", "repeatable"), true);
		Schemas.property(comment, "text",
			Schemas.string("Comment text, up to 64 KiB. Empty clears the comment."), true);
		revisionParam(comment);

		return List.of(
			new Operation(RENAME,
				"Renames the function or label at an address. Recorded in history and revertible.",
				OperationClass.WRITE, rename, Schemas.any("New revision and history entry."),
				(ctx, params) -> {
					String newName = Params.requireString(params, "newName");
					JsonObject target = new JsonObject();
					target.addProperty("address", Params.requireString(params, "address"));
					return change(services, ctx, RENAME, params, target, new JsonPrimitive(newName),
						null, null);
				}),
			new Operation(COMMENT,
				"Sets or clears a comment at an address. Recorded in history and revertible.",
				OperationClass.WRITE, comment, Schemas.any("New revision and history entry."),
				(ctx, params) -> {
					String text = Params.string(params, "text");
					if (text != null && text.length() > MAX_COMMENT_CHARS) {
						throw new RpcException(ErrorCode.INVALID_PARAMS,
							"text is longer than 64 KiB.");
					}
					JsonObject target = new JsonObject();
					target.addProperty("address", Params.requireString(params, "address"));
					target.addProperty("kind", Params.requireString(params, "kind"));
					JsonElement value = text == null || text.isEmpty() ? JsonNull.INSTANCE
							: new JsonPrimitive(text);
					return change(services, ctx, COMMENT, params, target, value, null, null);
				}));
	}

	static void revisionParam(JsonObject schema) {
		Schemas.property(schema, "expectedRevision",
			Schemas.integer("The program revision you last read; stale writes are refused.", 0,
				Long.MAX_VALUE),
			true);
	}

	/**
	 * Applies one change and journals it. When {@code requireCurrent} is set, the current value
	 * must equal it or the change is refused with CONFLICT (used by revert).
	 */
	static JsonObject change(EngineServices services, CallContext ctx, String op,
			JsonObject params, JsonObject target, JsonElement newValue, JsonElement requireCurrent,
			String revertOf) throws RpcException {
		String programId = Params.requireString(params, "programId");
		long expected = Params.requireLong(params, "expectedRevision");
		String[] shown = new String[2];
		JsonElement[] current = new JsonElement[1];
		// Every refusal happens before the transaction, so a refused write changes nothing,
		// not even the revision.
		Workspace.Precheck precheck = (program, revision) -> {
			if (revision != expected) {
				throw new RpcException(ErrorCode.STALE_REVISION,
					"The program changed since revision " + expected + " (now " + revision +
						"). Read it again.");
			}
			current[0] = currentValue(program, op, target);
			if (requireCurrent != null && !current[0].equals(requireCurrent)) {
				throw new RpcException(ErrorCode.CONFLICT,
					"The value changed after this edit was made; nothing was reverted.");
			}
			validate(op, newValue);
			shown[0] = displayValue(program, op, target);
		};
		Workspace.Written<JsonElement> written =
			services.workspace().write(programId, describe(op), precheck, program -> {
				applyValue(program, op, target, newValue);
				shown[1] = displayValue(program, op, target);
				return current[0];
			});
		JournalEntry entry = new JournalEntry(JournalEntry.newId(), JournalEntry.now(), ctx.actor(),
			op, programId, written.revisionBefore(), written.revisionAfter(), target,
			written.value(), newValue, revertOf, summary(op, target, shown[0], shown[1], revertOf));
		append(services, entry);
		JsonObject out = new JsonObject();
		out.addProperty("revision", written.revisionAfter());
		out.addProperty("entryId", entry.id());
		return out;
	}

	/** Appends a journal entry for a change that has already been applied. */
	static void append(EngineServices services, JournalEntry entry) throws RpcException {
		append(services.journal(), entry);
	}

	static void append(io.opcodex.engine.journal.Journal journal, JournalEntry entry)
			throws RpcException {
		try {
			journal.append(entry);
		}
		catch (IOException e) {
			Msg.error(EditOps.class, "Change applied but not journaled: " + entry.toJson(), e);
			throw new RpcException(ErrorCode.ENGINE_ERROR,
				"The change was applied, but Opcodex couldn't record it in history.");
		}
	}

	private static void validate(String op, JsonElement value) throws RpcException {
		if (!op.equals(RENAME) || value == null || value.isJsonNull()) {
			return;
		}
		try {
			SymbolUtilities.validateName(value.getAsString());
		}
		catch (InvalidInputException e) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, "Invalid name: " + e.getMessage());
		}
	}

	private static String summary(String op, JsonObject target, String before, String after,
			String revertOf) {
		String verb = revertOf != null ? "revert " : "";
		if (op.equals(RENAME)) {
			return verb + "rename " + before + " → " + after;
		}
		String kind = target.get("kind").getAsString();
		return verb + kind + " comment at " + target.get("address").getAsString() + ": " +
			clip(before) + " → " + clip(after);
	}

	private static String clip(String text) {
		if (text == null) {
			return "(none)";
		}
		String flat = text.replace('\n', ' ');
		return flat.length() <= 60 ? "\"" + flat + "\"" : "\"" + flat.substring(0, 60) + "…\"";
	}

	/** What a person sees at the target now, including default names. */
	private static String displayValue(Program program, String op, JsonObject target)
			throws RpcException {
		Address address = Params.address(program, "address", target.get("address").getAsString());
		if (op.equals(COMMENT)) {
			return program.getListing().getComment(commentType(target), address);
		}
		Function function = program.getFunctionManager().getFunctionAt(address);
		if (function != null) {
			return function.getName(true);
		}
		Symbol symbol = program.getSymbolTable().getPrimarySymbol(address);
		return symbol == null ? address.toString() : symbol.getName(true);
	}

	private static String describe(String op) {
		return op.equals(RENAME) ? "Rename" : "Set comment";
	}

	/** The current value of a change target; JsonNull means the default (no name, no comment). */
	static JsonElement currentValue(Program program, String op, JsonObject target)
			throws RpcException {
		Address address = Params.address(program, "address", target.get("address").getAsString());
		if (op.equals(RENAME)) {
			Function function = program.getFunctionManager().getFunctionAt(address);
			Symbol symbol = function != null ? function.getSymbol()
					: program.getSymbolTable().getPrimarySymbol(address);
			if (symbol == null || symbol.isDynamic() || symbol.getSource() == SourceType.DEFAULT) {
				return JsonNull.INSTANCE;
			}
			return new JsonPrimitive(symbol.getName());
		}
		String comment = program.getListing().getComment(commentType(target), address);
		return comment == null ? JsonNull.INSTANCE : new JsonPrimitive(comment);
	}

	static void applyValue(Program program, String op, JsonObject target, JsonElement value)
			throws RpcException {
		Address address = Params.address(program, "address", target.get("address").getAsString());
		String text = value == null || value.isJsonNull() ? null : value.getAsString();
		if (op.equals(COMMENT)) {
			program.getListing().setComment(address, commentType(target), text);
			return;
		}
		try {
			Function function = program.getFunctionManager().getFunctionAt(address);
			if (function != null) {
				function.setName(text, text == null ? SourceType.DEFAULT : SourceType.USER_DEFINED);
				return;
			}
			SymbolTable symbols = program.getSymbolTable();
			Symbol symbol = symbols.getPrimarySymbol(address);
			if (text == null) {
				if (symbol != null && !symbol.isDynamic()) {
					symbol.delete();
				}
			}
			else if (symbol == null || symbol.isDynamic()) {
				symbols.createLabel(address, text, SourceType.USER_DEFINED);
			}
			else {
				symbol.setName(text, SourceType.USER_DEFINED);
			}
		}
		catch (InvalidInputException e) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, "Invalid name: " + e.getMessage());
		}
		catch (DuplicateNameException e) {
			throw new RpcException(ErrorCode.CONFLICT, "That name is already used: " + text);
		}
	}

	private static CommentType commentType(JsonObject target) {
		return CommentType.valueOf(target.get("kind").getAsString().toUpperCase(Locale.ROOT));
	}
}
