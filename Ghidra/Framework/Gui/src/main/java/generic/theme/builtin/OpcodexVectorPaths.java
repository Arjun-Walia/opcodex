/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package generic.theme.builtin;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.concurrent.*;
/** Generated original geometry. No SVG parser or bitmap scaling at runtime. */
final class OpcodexVectorPaths {
	record Part(Shape shape, boolean fill, boolean stroke, float[] dash) {}
	private static final ConcurrentMap<String, Part[]> CACHE = new ConcurrentHashMap<>();
	static Part[] get(String name) { return CACHE.computeIfAbsent(name, OpcodexVectorPaths::create); }
	private static Part[] create(String name) { return switch (name) {
		case "file" -> glyph0();
		case "folder" -> glyph1();
		case "folder-open" -> glyph2();
		case "folder-add" -> glyph3();
		case "save" -> glyph4();
		case "copy" -> glyph5();
		case "cut" -> glyph6();
		case "paste" -> glyph7();
		case "plus" -> glyph8();
		case "minus" -> glyph9();
		case "close" -> glyph10();
		case "trash" -> glyph11();
		case "search" -> glyph12();
		case "filter" -> glyph13();
		case "filter-off" -> glyph14();
		case "settings" -> glyph15();
		case "left" -> glyph16();
		case "right" -> glyph17();
		case "up" -> glyph18();
		case "down" -> glyph19();
		case "undo" -> glyph20();
		case "redo" -> glyph21();
		case "refresh" -> glyph22();
		case "home" -> glyph23();
		case "lock" -> glyph24();
		case "unlock" -> glyph25();
		case "check" -> glyph26();
		case "warning" -> glyph27();
		case "error" -> glyph28();
		case "info" -> glyph29();
		case "help" -> glyph30();
		case "stop" -> glyph31();
		case "play" -> glyph32();
		case "expand" -> glyph33();
		case "collapse" -> glyph34();
		case "table" -> glyph35();
		case "terminal" -> glyph36();
		case "function" -> glyph37();
		case "code" -> glyph38();
		case "braces" -> glyph39();
		case "cube" -> glyph40();
		case "variable" -> glyph41();
		case "parameter" -> glyph42();
		case "library" -> glyph43();
		case "tag" -> glyph44();
		case "layers" -> glyph45();
		case "bookmark" -> glyph46();
		case "pin" -> glyph47();
		case "link" -> glyph48();
		case "graph" -> glyph49();
		case "wand" -> glyph50();
		case "edit" -> glyph51();
		case "selection" -> glyph52();
		case "history" -> glyph53();
		case "camera" -> glyph54();
		case "sort-up" -> glyph55();
		case "sort-down" -> glyph56();
		case "import" -> glyph57();
		case "export" -> glyph58();
		case "bug" -> glyph59();
		case "drive" -> glyph60();
		case "zoom-in" -> glyph61();
		case "zoom-out" -> glyph62();
		case "flag" -> glyph63();
		case "grid" -> glyph64();
		case "book" -> glyph65();
		case "array" -> glyph66();
		case "cpu" -> glyph67();
		case "memory" -> glyph68();
		case "pause" -> glyph69();
		case "step-into" -> glyph70();
		case "step-over" -> glyph71();
		case "step-out" -> glyph72();
		case "step-back" -> glyph73();
		case "rewind" -> glyph74();
		case "fast-forward" -> glyph75();
		case "record" -> glyph76();
		case "breakpoint" -> glyph77();
		case "breakpoint-disabled" -> glyph78();
		case "breakpoint-mixed" -> glyph79();
		case "breakpoint-pending" -> glyph80();
		case "connect" -> glyph81();
		case "disconnect" -> glyph82();
		case "split" -> glyph83();
		case "merge" -> glyph84();
		case "compare" -> glyph85();
		case "branch" -> glyph86();
		case "clock" -> glyph87();
		case "image" -> glyph88();
		case "monitor" -> glyph89();
		case "phone" -> glyph90();
		case "volume" -> glyph91();
		case "archive" -> glyph92();
		case "binary" -> glyph93();
		case "hex" -> glyph94();
		case "text" -> glyph95();
		case "regex" -> glyph96();
		case "key" -> glyph97();
		case "star" -> glyph98();
		case "user" -> glyph99();
		case "users" -> glyph100();
		case "target" -> glyph101();
		case "eye" -> glyph102();
		case "move" -> glyph103();
		case "pointer" -> glyph104();
		case "list" -> glyph105();
		case "lightbulb" -> glyph106();
		case "wave" -> glyph107();
		case "asterisk" -> glyph108();
		case "circle" -> glyph109();
		case "circle-check" -> glyph110();
		case "circle-minus" -> glyph111();
		case "tree" -> glyph112();
		case "swap" -> glyph113();
		case "rss" -> glyph114();
		case "usb" -> glyph115();
		case "fit" -> glyph116();
		case "chevron-down" -> glyph117();
		case "arrow-up-left" -> glyph118();
		case "arrow-down-right" -> glyph119();
		case "palette" -> glyph120();
		case "clear-console" -> glyph121();
		case "graph-snapshot" -> glyph122();
		case "listing" -> glyph123();
		case "decompile" -> glyph124();
		case "instruction" -> glyph125();
		case "selection-next" -> glyph126();
		case "selection-previous" -> glyph127();
		case "highlight-next" -> glyph128();
		case "highlight-previous" -> glyph129();
		case "register-bank" -> glyph130();
		case "next-data" -> glyph131();
		case "api-reference" -> glyph132();
		case "editor-ide" -> glyph133();
		case "editor-external" -> glyph134();
		case "rename" -> glyph135();
		case "run-again" -> glyph136();
		case "filter-clear" -> glyph137();
		case "filter-tree-contains" -> glyph138();
		case "filter-tree-exact" -> glyph139();
		case "filter-tree-starts" -> glyph140();
		case "filter-tree-regex" -> glyph141();
		case "next-bookmark" -> glyph142();
		case "graph-cycle" -> glyph143();
		case "function-compare" -> glyph144();
		default -> throw new IllegalArgumentException("Unknown glyph: " + name);
	}; }
	private static Part[] glyph0() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(14.0, 3.0);
		p0.lineTo(6.0, 3.0);
		p0.curveTo(4.895431, 3.0, 4.0, 3.895431, 4.0, 5.0);
		p0.lineTo(4.0, 19.0);
		p0.curveTo(4.0, 20.104569, 4.895431, 21.0, 6.0, 21.0);
		p0.lineTo(18.0, 21.0);
		p0.curveTo(19.104569, 21.0, 20.0, 20.104569, 20.0, 19.0);
		p0.lineTo(20.0, 9.0);
		p0.lineTo(14.0, 3.0);
		p0.closePath();
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(14.0, 3.0);
		p1.lineTo(14.0, 9.0);
		p1.lineTo(20.0, 9.0);
		p1.moveTo(8.0, 13.0);
		p1.lineTo(16.0, 13.0);
		p1.moveTo(8.0, 17.0);
		p1.lineTo(13.0, 17.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph1() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 7.0);
		p0.lineTo(3.0, 5.0);
		p0.curveTo(3.0, 3.895431, 3.895431, 3.0, 5.0, 3.0);
		p0.lineTo(10.0, 3.0);
		p0.lineTo(13.0, 6.0);
		p0.lineTo(19.0, 6.0);
		p0.curveTo(20.104569, 6.0, 21.0, 6.895431, 21.0, 8.0);
		p0.lineTo(21.0, 19.0);
		p0.curveTo(21.0, 20.104569, 20.104569, 21.0, 19.0, 21.0);
		p0.lineTo(5.0, 21.0);
		p0.curveTo(3.895431, 21.0, 3.0, 20.104569, 3.0, 19.0);
		p0.lineTo(3.0, 7.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph2() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 17.0);
		p0.lineTo(3.0, 5.0);
		p0.curveTo(3.0, 3.895431, 3.895431, 3.0, 5.0, 3.0);
		p0.lineTo(10.0, 3.0);
		p0.lineTo(13.0, 6.0);
		p0.lineTo(19.0, 6.0);
		p0.curveTo(20.104569, 6.0, 21.0, 6.895431, 21.0, 8.0);
		p0.lineTo(21.0, 10.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(3.0, 11.0);
		p1.lineTo(21.0, 11.0);
		p1.lineTo(18.0, 21.0);
		p1.lineTo(3.0, 21.0);
		p1.lineTo(3.0, 11.0);
		p1.closePath();
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph3() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 12.0);
		p0.lineTo(3.0, 5.0);
		p0.curveTo(3.0, 3.895431, 3.895431, 3.0, 5.0, 3.0);
		p0.lineTo(10.0, 3.0);
		p0.lineTo(13.0, 6.0);
		p0.lineTo(19.0, 6.0);
		p0.curveTo(20.104569, 6.0, 21.0, 6.895431, 21.0, 8.0);
		p0.lineTo(21.0, 12.0);
		p0.moveTo(3.0, 16.0);
		p0.lineTo(3.0, 19.0);
		p0.curveTo(3.0, 20.104569, 3.895431, 21.0, 5.0, 21.0);
		p0.lineTo(11.0, 21.0);
		p0.moveTo(17.0, 14.0);
		p0.lineTo(17.0, 22.0);
		p0.moveTo(13.0, 18.0);
		p0.lineTo(21.0, 18.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph4() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(5.0, 3.0);
		p0.lineTo(17.0, 3.0);
		p0.lineTo(21.0, 7.0);
		p0.lineTo(21.0, 19.0);
		p0.curveTo(21.0, 20.104569, 20.104569, 21.0, 19.0, 21.0);
		p0.lineTo(5.0, 21.0);
		p0.curveTo(3.895431, 21.0, 3.0, 20.104569, 3.0, 19.0);
		p0.lineTo(3.0, 5.0);
		p0.curveTo(3.0, 3.895431, 3.895431, 3.0, 5.0, 3.0);
		p0.closePath();
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(7.0, 3.0);
		p1.lineTo(7.0, 9.0);
		p1.lineTo(16.0, 9.0);
		p1.lineTo(16.0, 3.0);
		p1.moveTo(7.0, 21.0);
		p1.lineTo(7.0, 14.0);
		p1.lineTo(17.0, 14.0);
		p1.lineTo(17.0, 21.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph5() {
		Shape p0 = new RoundRectangle2D.Double(8.0, 8.0, 13.0, 13.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(15.0, 8.0);
		p1.lineTo(15.0, 5.0);
		p1.curveTo(15.0, 3.895431, 14.104569, 3.0, 13.0, 3.0);
		p1.lineTo(5.0, 3.0);
		p1.curveTo(3.895431, 3.0, 3.0, 3.895431, 3.0, 5.0);
		p1.lineTo(3.0, 13.0);
		p1.curveTo(3.0, 14.104569, 3.895431, 15.0, 5.0, 15.0);
		p1.lineTo(8.0, 15.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph6() {
		Shape p0 = new Ellipse2D.Double(2.0, 3.0, 6.0, 6.0);
		Shape p1 = new Ellipse2D.Double(2.0, 15.0, 6.0, 6.0);
		Path2D.Double p2 = new Path2D.Double();
		p2.moveTo(7.5, 8.0);
		p2.lineTo(20.5, 21.0);
		p2.moveTo(7.5, 16.0);
		p2.lineTo(21.0, 3.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null)};
	}
	private static Part[] glyph7() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 5.0);
		p0.lineTo(5.0, 5.0);
		p0.lineTo(5.0, 21.0);
		p0.lineTo(19.0, 21.0);
		p0.lineTo(19.0, 5.0);
		p0.lineTo(16.0, 5.0);
		Shape p1 = new RoundRectangle2D.Double(8.0, 2.0, 8.0, 6.0, 4.0, 4.0);
		Path2D.Double p2 = new Path2D.Double();
		p2.moveTo(8.0, 12.0);
		p2.lineTo(16.0, 12.0);
		p2.moveTo(8.0, 16.0);
		p2.lineTo(14.0, 16.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null)};
	}
	private static Part[] glyph8() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 4.0);
		p0.lineTo(12.0, 20.0);
		p0.moveTo(4.0, 12.0);
		p0.lineTo(20.0, 12.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph9() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(4.0, 12.0);
		p0.lineTo(20.0, 12.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph10() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(6.0, 6.0);
		p0.lineTo(18.0, 18.0);
		p0.moveTo(6.0, 18.0);
		p0.lineTo(18.0, 6.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph11() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 6.0);
		p0.lineTo(21.0, 6.0);
		p0.moveTo(9.0, 6.0);
		p0.lineTo(9.0, 3.0);
		p0.lineTo(15.0, 3.0);
		p0.lineTo(15.0, 6.0);
		p0.moveTo(5.0, 6.0);
		p0.lineTo(6.0, 21.0);
		p0.lineTo(18.0, 21.0);
		p0.lineTo(19.0, 6.0);
		p0.moveTo(10.0, 10.0);
		p0.lineTo(10.0, 17.0);
		p0.moveTo(14.0, 10.0);
		p0.lineTo(14.0, 17.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph12() {
		Shape p0 = new Ellipse2D.Double(4.0, 4.0, 13.0, 13.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(16.0, 16.0);
		p1.lineTo(21.0, 21.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph13() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 4.0);
		p0.lineTo(21.0, 4.0);
		p0.lineTo(14.0, 12.0);
		p0.lineTo(14.0, 19.0);
		p0.lineTo(10.0, 21.0);
		p0.lineTo(10.0, 12.0);
		p0.lineTo(3.0, 4.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph14() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 4.0);
		p0.lineTo(21.0, 4.0);
		p0.lineTo(14.0, 12.0);
		p0.lineTo(14.0, 17.0);
		p0.moveTo(10.0, 13.0);
		p0.lineTo(10.0, 19.0);
		p0.lineTo(14.0, 21.0);
		p0.moveTo(3.0, 3.0);
		p0.lineTo(21.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph15() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(4.0, 6.0);
		p0.lineTo(10.0, 6.0);
		p0.moveTo(14.0, 6.0);
		p0.lineTo(20.0, 6.0);
		p0.moveTo(4.0, 12.0);
		p0.lineTo(14.0, 12.0);
		p0.moveTo(18.0, 12.0);
		p0.lineTo(20.0, 12.0);
		p0.moveTo(4.0, 18.0);
		p0.lineTo(6.0, 18.0);
		p0.moveTo(10.0, 18.0);
		p0.lineTo(20.0, 18.0);
		Shape p1 = new Ellipse2D.Double(10.0, 4.0, 4.0, 4.0);
		Shape p2 = new Ellipse2D.Double(14.0, 10.0, 4.0, 4.0);
		Shape p3 = new Ellipse2D.Double(6.0, 16.0, 4.0, 4.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null), new Part(p3, false, true, null)};
	}
	private static Part[] glyph16() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 5.0);
		p0.lineTo(5.0, 12.0);
		p0.lineTo(12.0, 19.0);
		p0.moveTo(5.0, 12.0);
		p0.lineTo(21.0, 12.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph17() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 5.0);
		p0.lineTo(19.0, 12.0);
		p0.lineTo(12.0, 19.0);
		p0.moveTo(3.0, 12.0);
		p0.lineTo(19.0, 12.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph18() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(5.0, 12.0);
		p0.lineTo(12.0, 5.0);
		p0.lineTo(19.0, 12.0);
		p0.moveTo(12.0, 5.0);
		p0.lineTo(12.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph19() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(5.0, 12.0);
		p0.lineTo(12.0, 19.0);
		p0.lineTo(19.0, 12.0);
		p0.moveTo(12.0, 3.0);
		p0.lineTo(12.0, 19.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph20() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 4.0);
		p0.lineTo(3.0, 9.0);
		p0.lineTo(8.0, 14.0);
		p0.moveTo(3.0, 9.0);
		p0.lineTo(14.0, 9.0);
		p0.curveTo(17.313708, 9.0, 20.0, 11.686292, 20.0, 15.0);
		p0.curveTo(20.0, 18.313708, 17.313708, 21.0, 14.0, 21.0);
		p0.lineTo(11.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph21() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(16.0, 4.0);
		p0.lineTo(21.0, 9.0);
		p0.lineTo(16.0, 14.0);
		p0.moveTo(21.0, 9.0);
		p0.lineTo(10.0, 9.0);
		p0.curveTo(6.686292, 9.0, 4.0, 11.686292, 4.0, 15.0);
		p0.curveTo(4.0, 18.313708, 6.686292, 21.0, 10.0, 21.0);
		p0.lineTo(13.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph22() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(20.0, 8.0);
		p0.curveTo(19.244992, 5.062482, 16.893732, 2.804534, 13.928064, 2.169034);
		p0.curveTo(10.962396, 1.533533, 7.89238, 2.62978, 6.0, 5.0);
		p0.lineTo(3.0, 8.0);
		p0.moveTo(3.0, 3.0);
		p0.lineTo(3.0, 8.0);
		p0.lineTo(8.0, 8.0);
		p0.moveTo(4.0, 16.0);
		p0.curveTo(4.755008, 18.937518, 7.106268, 21.195466, 10.071936, 21.830966);
		p0.curveTo(13.037604, 22.466467, 16.10762, 21.37022, 18.0, 19.0);
		p0.lineTo(21.0, 16.0);
		p0.moveTo(16.0, 16.0);
		p0.lineTo(21.0, 16.0);
		p0.lineTo(21.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph23() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 10.0);
		p0.lineTo(12.0, 3.0);
		p0.lineTo(21.0, 10.0);
		p0.moveTo(5.0, 9.0);
		p0.lineTo(5.0, 21.0);
		p0.lineTo(19.0, 21.0);
		p0.lineTo(19.0, 9.0);
		p0.moveTo(10.0, 21.0);
		p0.lineTo(10.0, 14.0);
		p0.lineTo(14.0, 14.0);
		p0.lineTo(14.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph24() {
		Shape p0 = new RoundRectangle2D.Double(5.0, 10.0, 14.0, 11.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(8.0, 10.0);
		p1.lineTo(8.0, 7.0);
		p1.curveTo(8.0, 4.790861, 9.790861, 3.0, 12.0, 3.0);
		p1.curveTo(14.209139, 3.0, 16.0, 4.790861, 16.0, 7.0);
		p1.lineTo(16.0, 10.0);
		p1.moveTo(12.0, 14.0);
		p1.lineTo(12.0, 17.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph25() {
		Shape p0 = new RoundRectangle2D.Double(5.0, 10.0, 14.0, 11.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(8.0, 10.0);
		p1.lineTo(8.0, 7.0);
		p1.curveTo(8.232949, 5.450068, 9.349654, 4.178993, 10.856689, 3.748411);
		p1.curveTo(12.363724, 3.31783, 13.983381, 3.807087, 15.0, 5.0);
		p1.moveTo(12.0, 14.0);
		p1.lineTo(12.0, 17.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph26() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(4.0, 12.0);
		p0.lineTo(9.0, 17.0);
		p0.lineTo(20.0, 6.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph27() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 3.0);
		p0.lineTo(2.0, 21.0);
		p0.lineTo(22.0, 21.0);
		p0.lineTo(12.0, 3.0);
		p0.closePath();
		p0.moveTo(12.0, 9.0);
		p0.lineTo(12.0, 14.0);
		Shape p1 = new Ellipse2D.Double(11.4, 16.9, 1.2, 1.2);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, true, true, null)};
	}
	private static Part[] glyph28() {
		Shape p0 = new Ellipse2D.Double(3.0, 3.0, 18.0, 18.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(9.0, 9.0);
		p1.lineTo(15.0, 15.0);
		p1.moveTo(9.0, 15.0);
		p1.lineTo(15.0, 9.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph29() {
		Shape p0 = new Ellipse2D.Double(3.0, 3.0, 18.0, 18.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(12.0, 11.0);
		p1.lineTo(12.0, 17.0);
		Shape p2 = new Ellipse2D.Double(11.4, 6.9, 1.2, 1.2);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, true, true, null)};
	}
	private static Part[] glyph30() {
		Shape p0 = new Ellipse2D.Double(3.0, 3.0, 18.0, 18.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(9.0, 9.0);
		p1.curveTo(9.0, 7.343146, 10.343146, 6.0, 12.0, 6.0);
		p1.curveTo(13.656854, 6.0, 15.0, 7.343146, 15.0, 9.0);
		p1.curveTo(15.0, 11.0, 12.0, 11.0, 12.0, 14.0);
		Shape p2 = new Ellipse2D.Double(11.4, 16.9, 1.2, 1.2);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, true, true, null)};
	}
	private static Part[] glyph31() {
		Shape p0 = new RoundRectangle2D.Double(5.0, 5.0, 14.0, 14.0, 4.0, 4.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph32() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(7.0, 3.0);
		p0.lineTo(21.0, 12.0);
		p0.lineTo(7.0, 21.0);
		p0.lineTo(7.0, 3.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph33() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 6.0);
		p0.lineTo(12.0, 2.0);
		p0.lineTo(16.0, 6.0);
		p0.moveTo(8.0, 18.0);
		p0.lineTo(12.0, 22.0);
		p0.lineTo(16.0, 18.0);
		p0.moveTo(12.0, 2.0);
		p0.lineTo(12.0, 9.0);
		p0.moveTo(12.0, 15.0);
		p0.lineTo(12.0, 22.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph34() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 3.0);
		p0.lineTo(12.0, 7.0);
		p0.lineTo(16.0, 3.0);
		p0.moveTo(8.0, 21.0);
		p0.lineTo(12.0, 17.0);
		p0.lineTo(16.0, 21.0);
		p0.moveTo(12.0, 2.0);
		p0.lineTo(12.0, 7.0);
		p0.moveTo(12.0, 17.0);
		p0.lineTo(12.0, 22.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph35() {
		Shape p0 = new RoundRectangle2D.Double(3.0, 3.0, 18.0, 18.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(3.0, 9.0);
		p1.lineTo(21.0, 9.0);
		p1.moveTo(3.0, 15.0);
		p1.lineTo(21.0, 15.0);
		p1.moveTo(9.0, 9.0);
		p1.lineTo(9.0, 21.0);
		p1.moveTo(15.0, 9.0);
		p1.lineTo(15.0, 21.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph36() {
		Shape p0 = new RoundRectangle2D.Double(3.0, 4.0, 18.0, 16.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(7.0, 8.0);
		p1.lineTo(11.0, 12.0);
		p1.lineTo(7.0, 16.0);
		p1.moveTo(13.0, 16.0);
		p1.lineTo(17.0, 16.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph37() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(16.0, 3.0);
		p0.lineTo(14.0, 3.0);
		p0.curveTo(12.343146, 3.0, 11.0, 4.343146, 11.0, 6.0);
		p0.lineTo(9.0, 18.0);
		p0.curveTo(9.0, 19.656854, 7.656854, 21.0, 6.0, 21.0);
		p0.lineTo(4.0, 21.0);
		p0.moveTo(6.0, 9.0);
		p0.lineTo(17.0, 9.0);
		p0.moveTo(15.0, 15.0);
		p0.lineTo(21.0, 21.0);
		p0.moveTo(15.0, 21.0);
		p0.lineTo(21.0, 15.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph38() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 6.0);
		p0.lineTo(2.0, 12.0);
		p0.lineTo(8.0, 18.0);
		p0.moveTo(16.0, 6.0);
		p0.lineTo(22.0, 12.0);
		p0.lineTo(16.0, 18.0);
		p0.moveTo(14.0, 3.0);
		p0.lineTo(10.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph39() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(9.0, 3.0);
		p0.lineTo(7.0, 3.0);
		p0.curveTo(5.895431, 3.0, 5.0, 3.895431, 5.0, 5.0);
		p0.lineTo(5.0, 9.0);
		p0.lineTo(3.0, 12.0);
		p0.lineTo(5.0, 15.0);
		p0.lineTo(5.0, 19.0);
		p0.curveTo(5.0, 20.104569, 5.895431, 21.0, 7.0, 21.0);
		p0.lineTo(9.0, 21.0);
		p0.moveTo(15.0, 3.0);
		p0.lineTo(17.0, 3.0);
		p0.curveTo(18.104569, 3.0, 19.0, 3.895431, 19.0, 5.0);
		p0.lineTo(19.0, 9.0);
		p0.lineTo(21.0, 12.0);
		p0.lineTo(19.0, 15.0);
		p0.lineTo(19.0, 19.0);
		p0.curveTo(19.0, 20.104569, 18.104569, 21.0, 17.0, 21.0);
		p0.lineTo(15.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph40() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 2.0);
		p0.lineTo(21.0, 7.0);
		p0.lineTo(21.0, 17.0);
		p0.lineTo(12.0, 22.0);
		p0.lineTo(3.0, 17.0);
		p0.lineTo(3.0, 7.0);
		p0.lineTo(12.0, 2.0);
		p0.closePath();
		p0.moveTo(3.0, 7.0);
		p0.lineTo(12.0, 12.0);
		p0.lineTo(21.0, 7.0);
		p0.moveTo(12.0, 12.0);
		p0.lineTo(12.0, 22.0);
		p0.moveTo(7.5, 4.5);
		p0.lineTo(16.5, 9.5);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph41() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 6.0);
		p0.lineTo(8.0, 19.0);
		p0.lineTo(10.0, 19.0);
		p0.lineTo(15.0, 6.0);
		p0.moveTo(16.0, 4.0);
		p0.lineTo(21.0, 4.0);
		p0.moveTo(18.5, 1.5);
		p0.lineTo(18.5, 6.5);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph42() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(6.0, 21.0);
		p0.lineTo(6.0, 4.0);
		p0.lineTo(13.0, 4.0);
		p0.curveTo(15.761424, 4.0, 18.0, 6.238576, 18.0, 9.0);
		p0.curveTo(18.0, 11.761424, 15.761424, 14.0, 13.0, 14.0);
		p0.lineTo(6.0, 14.0);
		p0.moveTo(16.0, 19.0);
		p0.lineTo(22.0, 19.0);
		p0.moveTo(19.0, 16.0);
		p0.lineTo(19.0, 22.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph43() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 4.0);
		p0.lineTo(7.0, 4.0);
		p0.lineTo(7.0, 21.0);
		p0.lineTo(3.0, 21.0);
		p0.lineTo(3.0, 4.0);
		p0.closePath();
		p0.moveTo(10.0, 4.0);
		p0.lineTo(14.0, 4.0);
		p0.lineTo(14.0, 21.0);
		p0.lineTo(10.0, 21.0);
		p0.lineTo(10.0, 4.0);
		p0.closePath();
		p0.moveTo(16.0, 5.0);
		p0.lineTo(20.0, 4.0);
		p0.lineTo(23.0, 20.0);
		p0.lineTo(19.0, 21.0);
		p0.lineTo(16.0, 5.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph44() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 3.0);
		p0.lineTo(11.0, 3.0);
		p0.lineTo(21.0, 13.0);
		p0.lineTo(13.0, 21.0);
		p0.lineTo(3.0, 11.0);
		p0.lineTo(3.0, 3.0);
		p0.closePath();
		Shape p1 = new Ellipse2D.Double(6.0, 6.0, 2.0, 2.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, true, true, null)};
	}
	private static Part[] glyph45() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 2.0);
		p0.lineTo(22.0, 8.0);
		p0.lineTo(12.0, 14.0);
		p0.lineTo(2.0, 8.0);
		p0.lineTo(12.0, 2.0);
		p0.closePath();
		p0.moveTo(2.0, 12.0);
		p0.lineTo(12.0, 18.0);
		p0.lineTo(22.0, 12.0);
		p0.moveTo(2.0, 16.0);
		p0.lineTo(12.0, 22.0);
		p0.lineTo(22.0, 16.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph46() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(6.0, 3.0);
		p0.lineTo(18.0, 3.0);
		p0.lineTo(18.0, 22.0);
		p0.lineTo(12.0, 18.0);
		p0.lineTo(6.0, 22.0);
		p0.lineTo(6.0, 3.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph47() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(15.0, 3.0);
		p0.lineTo(21.0, 9.0);
		p0.lineTo(17.0, 10.0);
		p0.lineTo(13.0, 17.0);
		p0.lineTo(10.0, 14.0);
		p0.lineTo(3.0, 21.0);
		p0.moveTo(10.0, 14.0);
		p0.lineTo(7.0, 11.0);
		p0.lineTo(14.0, 7.0);
		p0.lineTo(10.0, 14.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph48() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(10.0, 7.0);
		p0.lineTo(13.0, 4.0);
		p0.curveTo(14.990602, 2.507049, 17.776074, 2.705006, 19.535534, 4.464466);
		p0.curveTo(21.294994, 6.223926, 21.492951, 9.009398, 20.0, 11.0);
		p0.lineTo(17.0, 14.0);
		p0.moveTo(14.0, 17.0);
		p0.lineTo(11.0, 20.0);
		p0.curveTo(9.009398, 21.492951, 6.223926, 21.294994, 4.464466, 19.535534);
		p0.curveTo(2.705006, 17.776074, 2.507049, 14.990602, 4.0, 13.0);
		p0.lineTo(7.0, 10.0);
		p0.moveTo(8.0, 16.0);
		p0.lineTo(16.0, 8.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph49() {
		Shape p0 = new RoundRectangle2D.Double(9.0, 2.0, 6.0, 6.0, 2.0, 2.0);
		Shape p1 = new RoundRectangle2D.Double(2.0, 16.0, 6.0, 6.0, 2.0, 2.0);
		Shape p2 = new RoundRectangle2D.Double(16.0, 16.0, 6.0, 6.0, 2.0, 2.0);
		Path2D.Double p3 = new Path2D.Double();
		p3.moveTo(12.0, 8.0);
		p3.lineTo(12.0, 12.0);
		p3.moveTo(5.0, 16.0);
		p3.lineTo(5.0, 12.0);
		p3.lineTo(19.0, 12.0);
		p3.lineTo(19.0, 16.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null), new Part(p3, false, true, null)};
	}
	private static Part[] glyph50() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(4.0, 21.0);
		p0.lineTo(16.0, 9.0);
		p0.lineTo(19.0, 12.0);
		p0.lineTo(7.0, 24.0);
		p0.moveTo(14.0, 3.0);
		p0.lineTo(14.0, 6.0);
		p0.moveTo(20.0, 4.0);
		p0.lineTo(18.0, 6.0);
		p0.moveTo(21.0, 10.0);
		p0.lineTo(24.0, 10.0);
		p0.moveTo(4.0, 6.0);
		p0.lineTo(8.0, 6.0);
		p0.moveTo(6.0, 4.0);
		p0.lineTo(6.0, 8.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(13.0, 12.0);
		p1.lineTo(16.0, 15.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph51() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(4.0, 16.0);
		p0.lineTo(16.0, 4.0);
		p0.lineTo(20.0, 8.0);
		p0.lineTo(8.0, 20.0);
		p0.lineTo(3.0, 21.0);
		p0.lineTo(4.0, 16.0);
		p0.closePath();
		p0.moveTo(14.0, 6.0);
		p0.lineTo(18.0, 10.0);
		p0.moveTo(13.0, 21.0);
		p0.lineTo(21.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph52() {
		Shape p0 = new RoundRectangle2D.Double(4.0, 4.0, 16.0, 16.0, 2.0, 2.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(10.0, 10.0);
		p1.lineTo(19.0, 13.0);
		p1.lineTo(15.0, 15.0);
		p1.lineTo(13.0, 19.0);
		p1.lineTo(10.0, 10.0);
		p1.closePath();
		return new Part[]{new Part(p0, false, true, new float[]{3.0f,3.0f}), new Part(p1, false, true, null)};
	}
	private static Part[] glyph53() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 8.0);
		p0.curveTo(4.857422, 4.256246, 9.0476, 2.28368, 13.116483, 3.237571);
		p0.curveTo(17.185365, 4.191462, 20.062258, 7.8208, 20.062258, 12.0);
		p0.curveTo(20.062258, 16.1792, 17.185365, 19.808538, 13.116483, 20.762429);
		p0.curveTo(9.0476, 21.71632, 4.857422, 19.743754, 3.0, 16.0);
		p0.moveTo(3.0, 3.0);
		p0.lineTo(3.0, 8.0);
		p0.lineTo(8.0, 8.0);
		p0.moveTo(12.0, 7.0);
		p0.lineTo(12.0, 13.0);
		p0.lineTo(16.0, 15.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph54() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 7.0);
		p0.lineTo(7.0, 7.0);
		p0.lineTo(9.0, 4.0);
		p0.lineTo(15.0, 4.0);
		p0.lineTo(17.0, 7.0);
		p0.lineTo(21.0, 7.0);
		p0.lineTo(21.0, 21.0);
		p0.lineTo(3.0, 21.0);
		p0.lineTo(3.0, 7.0);
		p0.closePath();
		Shape p1 = new Ellipse2D.Double(8.0, 9.0, 8.0, 8.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph55() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(4.0, 8.0);
		p0.lineTo(8.0, 4.0);
		p0.lineTo(12.0, 8.0);
		p0.moveTo(8.0, 4.0);
		p0.lineTo(8.0, 20.0);
		p0.moveTo(15.0, 7.0);
		p0.lineTo(21.0, 7.0);
		p0.moveTo(15.0, 12.0);
		p0.lineTo(19.0, 12.0);
		p0.moveTo(15.0, 17.0);
		p0.lineTo(17.0, 17.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph56() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(4.0, 16.0);
		p0.lineTo(8.0, 20.0);
		p0.lineTo(12.0, 16.0);
		p0.moveTo(8.0, 4.0);
		p0.lineTo(8.0, 20.0);
		p0.moveTo(15.0, 7.0);
		p0.lineTo(21.0, 7.0);
		p0.moveTo(15.0, 12.0);
		p0.lineTo(19.0, 12.0);
		p0.moveTo(15.0, 17.0);
		p0.lineTo(17.0, 17.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph57() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(14.0, 3.0);
		p0.lineTo(21.0, 3.0);
		p0.lineTo(21.0, 21.0);
		p0.lineTo(14.0, 21.0);
		p0.moveTo(3.0, 12.0);
		p0.lineTo(16.0, 12.0);
		p0.moveTo(11.0, 7.0);
		p0.lineTo(16.0, 12.0);
		p0.lineTo(11.0, 17.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph58() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(10.0, 3.0);
		p0.lineTo(3.0, 3.0);
		p0.lineTo(3.0, 21.0);
		p0.lineTo(10.0, 21.0);
		p0.moveTo(8.0, 12.0);
		p0.lineTo(21.0, 12.0);
		p0.moveTo(16.0, 7.0);
		p0.lineTo(21.0, 12.0);
		p0.lineTo(16.0, 17.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph59() {
		Shape p0 = new RoundRectangle2D.Double(7.0, 7.0, 10.0, 14.0, 10.0, 10.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(9.0, 7.0);
		p1.lineTo(7.0, 3.0);
		p1.moveTo(15.0, 7.0);
		p1.lineTo(17.0, 3.0);
		p1.moveTo(3.0, 9.0);
		p1.lineTo(7.0, 11.0);
		p1.moveTo(17.0, 11.0);
		p1.lineTo(21.0, 9.0);
		p1.moveTo(2.0, 16.0);
		p1.lineTo(7.0, 16.0);
		p1.moveTo(17.0, 16.0);
		p1.lineTo(22.0, 16.0);
		p1.moveTo(5.0, 23.0);
		p1.lineTo(8.0, 19.0);
		p1.moveTo(16.0, 19.0);
		p1.lineTo(19.0, 23.0);
		p1.moveTo(12.0, 7.0);
		p1.lineTo(12.0, 21.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph60() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 14.0);
		p0.lineTo(6.0, 5.0);
		p0.lineTo(18.0, 5.0);
		p0.lineTo(21.0, 14.0);
		p0.lineTo(21.0, 21.0);
		p0.lineTo(3.0, 21.0);
		p0.lineTo(3.0, 14.0);
		p0.closePath();
		p0.moveTo(3.0, 14.0);
		p0.lineTo(21.0, 14.0);
		Shape p1 = new Ellipse2D.Double(16.4, 16.9, 1.2, 1.2);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, true, true, null)};
	}
	private static Part[] glyph61() {
		Shape p0 = new Ellipse2D.Double(3.0, 3.0, 14.0, 14.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(15.0, 15.0);
		p1.lineTo(21.0, 21.0);
		p1.moveTo(7.0, 10.0);
		p1.lineTo(13.0, 10.0);
		p1.moveTo(10.0, 7.0);
		p1.lineTo(10.0, 13.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph62() {
		Shape p0 = new Ellipse2D.Double(3.0, 3.0, 14.0, 14.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(15.0, 15.0);
		p1.lineTo(21.0, 21.0);
		p1.moveTo(7.0, 10.0);
		p1.lineTo(13.0, 10.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph63() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(5.0, 22.0);
		p0.lineTo(5.0, 3.0);
		p0.moveTo(5.0, 4.0);
		p0.curveTo(10.0, 0.0, 14.0, 8.0, 19.0, 4.0);
		p0.lineTo(19.0, 14.0);
		p0.curveTo(14.0, 18.0, 10.0, 10.0, 5.0, 14.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph64() {
		Shape p0 = new RoundRectangle2D.Double(3.0, 3.0, 7.0, 7.0, 2.0, 2.0);
		Shape p1 = new RoundRectangle2D.Double(14.0, 3.0, 7.0, 7.0, 2.0, 2.0);
		Shape p2 = new RoundRectangle2D.Double(3.0, 14.0, 7.0, 7.0, 2.0, 2.0);
		Shape p3 = new RoundRectangle2D.Double(14.0, 14.0, 7.0, 7.0, 2.0, 2.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null), new Part(p3, false, true, null)};
	}
	private static Part[] glyph65() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 5.0);
		p0.curveTo(8.0, 2.0, 5.0, 3.0, 2.0, 4.0);
		p0.lineTo(2.0, 20.0);
		p0.curveTo(6.0, 18.0, 9.0, 18.0, 12.0, 21.0);
		p0.curveTo(15.0, 18.0, 18.0, 18.0, 22.0, 20.0);
		p0.lineTo(22.0, 4.0);
		p0.curveTo(19.0, 3.0, 16.0, 2.0, 12.0, 5.0);
		p0.closePath();
		p0.moveTo(12.0, 5.0);
		p0.lineTo(12.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph66() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 3.0);
		p0.lineTo(4.0, 3.0);
		p0.lineTo(4.0, 21.0);
		p0.lineTo(8.0, 21.0);
		p0.moveTo(16.0, 3.0);
		p0.lineTo(20.0, 3.0);
		p0.lineTo(20.0, 21.0);
		p0.lineTo(16.0, 21.0);
		p0.moveTo(10.0, 9.0);
		p0.lineTo(14.0, 9.0);
		p0.moveTo(10.0, 15.0);
		p0.lineTo(14.0, 15.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph67() {
		Shape p0 = new RoundRectangle2D.Double(6.0, 6.0, 12.0, 12.0, 4.0, 4.0);
		Shape p1 = new RoundRectangle2D.Double(9.0, 9.0, 6.0, 6.0, 2.0, 2.0);
		Path2D.Double p2 = new Path2D.Double();
		p2.moveTo(9.0, 2.0);
		p2.lineTo(9.0, 6.0);
		p2.moveTo(15.0, 2.0);
		p2.lineTo(15.0, 6.0);
		p2.moveTo(9.0, 18.0);
		p2.lineTo(9.0, 22.0);
		p2.moveTo(15.0, 18.0);
		p2.lineTo(15.0, 22.0);
		p2.moveTo(2.0, 9.0);
		p2.lineTo(6.0, 9.0);
		p2.moveTo(2.0, 15.0);
		p2.lineTo(6.0, 15.0);
		p2.moveTo(18.0, 9.0);
		p2.lineTo(22.0, 9.0);
		p2.moveTo(18.0, 15.0);
		p2.lineTo(22.0, 15.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null)};
	}
	private static Part[] glyph68() {
		Shape p0 = new RoundRectangle2D.Double(3.0, 5.0, 18.0, 14.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(7.0, 9.0);
		p1.lineTo(7.0, 15.0);
		p1.moveTo(11.0, 9.0);
		p1.lineTo(11.0, 15.0);
		p1.moveTo(15.0, 9.0);
		p1.lineTo(15.0, 15.0);
		p1.moveTo(3.0, 12.0);
		p1.lineTo(1.0, 12.0);
		p1.moveTo(23.0, 12.0);
		p1.lineTo(21.0, 12.0);
		p1.moveTo(7.0, 19.0);
		p1.lineTo(7.0, 22.0);
		p1.moveTo(12.0, 19.0);
		p1.lineTo(12.0, 22.0);
		p1.moveTo(17.0, 19.0);
		p1.lineTo(17.0, 22.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph69() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(7.0, 4.0);
		p0.lineTo(10.0, 4.0);
		p0.lineTo(10.0, 20.0);
		p0.lineTo(7.0, 20.0);
		p0.lineTo(7.0, 4.0);
		p0.closePath();
		p0.moveTo(14.0, 4.0);
		p0.lineTo(17.0, 4.0);
		p0.lineTo(17.0, 20.0);
		p0.lineTo(14.0, 20.0);
		p0.lineTo(14.0, 4.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph70() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 3.0);
		p0.lineTo(12.0, 15.0);
		p0.moveTo(7.0, 10.0);
		p0.lineTo(12.0, 15.0);
		p0.lineTo(17.0, 10.0);
		p0.moveTo(5.0, 21.0);
		p0.lineTo(19.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph71() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 15.0);
		p0.curveTo(2.723858, 10.581722, 6.081722, 6.776142, 10.5, 6.5);
		p0.curveTo(14.918278, 6.223858, 18.723858, 9.581722, 19.0, 14.0);
		p0.moveTo(15.0, 10.0);
		p0.lineTo(19.0, 14.0);
		p0.lineTo(22.0, 10.0);
		p0.moveTo(8.0, 21.0);
		p0.lineTo(16.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph72() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 17.0);
		p0.lineTo(12.0, 5.0);
		p0.moveTo(7.0, 10.0);
		p0.lineTo(12.0, 5.0);
		p0.lineTo(17.0, 10.0);
		p0.moveTo(5.0, 21.0);
		p0.lineTo(19.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph73() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(20.0, 6.0);
		p0.lineTo(8.0, 6.0);
		p0.lineTo(8.0, 18.0);
		p0.moveTo(3.0, 13.0);
		p0.lineTo(8.0, 18.0);
		p0.lineTo(13.0, 13.0);
		p0.moveTo(3.0, 22.0);
		p0.lineTo(13.0, 22.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph74() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 4.0);
		p0.lineTo(3.0, 12.0);
		p0.lineTo(12.0, 20.0);
		p0.lineTo(12.0, 4.0);
		p0.closePath();
		p0.moveTo(21.0, 4.0);
		p0.lineTo(12.0, 12.0);
		p0.lineTo(21.0, 20.0);
		p0.lineTo(21.0, 4.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph75() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 4.0);
		p0.lineTo(12.0, 12.0);
		p0.lineTo(3.0, 20.0);
		p0.lineTo(3.0, 4.0);
		p0.closePath();
		p0.moveTo(12.0, 4.0);
		p0.lineTo(21.0, 12.0);
		p0.lineTo(12.0, 20.0);
		p0.lineTo(12.0, 4.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph76() {
		Shape p0 = new Ellipse2D.Double(4.0, 4.0, 16.0, 16.0);
		Shape p1 = new Ellipse2D.Double(8.0, 8.0, 8.0, 8.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, true, true, null)};
	}
	private static Part[] glyph77() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 3.0);
		p0.lineTo(16.0, 3.0);
		p0.lineTo(21.0, 8.0);
		p0.lineTo(21.0, 16.0);
		p0.lineTo(16.0, 21.0);
		p0.lineTo(8.0, 21.0);
		p0.lineTo(3.0, 16.0);
		p0.lineTo(3.0, 8.0);
		p0.lineTo(8.0, 3.0);
		p0.closePath();
		Shape p1 = new Ellipse2D.Double(9.0, 9.0, 6.0, 6.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, true, true, null)};
	}
	private static Part[] glyph78() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 3.0);
		p0.lineTo(16.0, 3.0);
		p0.lineTo(21.0, 8.0);
		p0.lineTo(21.0, 16.0);
		p0.lineTo(16.0, 21.0);
		p0.lineTo(8.0, 21.0);
		p0.lineTo(3.0, 16.0);
		p0.lineTo(3.0, 8.0);
		p0.lineTo(8.0, 3.0);
		p0.closePath();
		p0.moveTo(7.0, 17.0);
		p0.lineTo(17.0, 7.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph79() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 3.0);
		p0.lineTo(16.0, 3.0);
		p0.lineTo(21.0, 8.0);
		p0.lineTo(21.0, 16.0);
		p0.lineTo(16.0, 21.0);
		p0.lineTo(8.0, 21.0);
		p0.lineTo(3.0, 16.0);
		p0.lineTo(3.0, 8.0);
		p0.lineTo(8.0, 3.0);
		p0.closePath();
		p0.moveTo(12.0, 4.0);
		p0.lineTo(12.0, 20.0);
		p0.moveTo(12.0, 8.0);
		p0.lineTo(18.0, 8.0);
		p0.moveTo(12.0, 12.0);
		p0.lineTo(20.0, 12.0);
		p0.moveTo(12.0, 16.0);
		p0.lineTo(18.0, 16.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph80() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 3.0);
		p0.lineTo(16.0, 3.0);
		p0.lineTo(21.0, 8.0);
		p0.lineTo(21.0, 16.0);
		p0.lineTo(16.0, 21.0);
		p0.lineTo(8.0, 21.0);
		p0.lineTo(3.0, 16.0);
		p0.lineTo(3.0, 8.0);
		p0.lineTo(8.0, 3.0);
		p0.closePath();
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(12.0, 8.0);
		p1.lineTo(12.0, 13.0);
		p1.moveTo(12.0, 17.0);
		p1.lineTo(12.01, 17.0);
		return new Part[]{new Part(p0, false, true, new float[]{2.0f,3.0f}), new Part(p1, false, true, null)};
	}
	private static Part[] glyph81() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 3.0);
		p0.lineTo(8.0, 7.0);
		p0.moveTo(16.0, 3.0);
		p0.lineTo(16.0, 7.0);
		p0.moveTo(6.0, 7.0);
		p0.lineTo(18.0, 7.0);
		p0.lineTo(18.0, 12.0);
		p0.curveTo(18.0, 15.313708, 15.313708, 18.0, 12.0, 18.0);
		p0.curveTo(8.686292, 18.0, 6.0, 15.313708, 6.0, 12.0);
		p0.lineTo(6.0, 7.0);
		p0.closePath();
		p0.moveTo(12.0, 18.0);
		p0.lineTo(12.0, 22.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph82() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 3.0);
		p0.lineTo(21.0, 21.0);
		p0.moveTo(8.0, 3.0);
		p0.lineTo(8.0, 7.0);
		p0.moveTo(16.0, 3.0);
		p0.lineTo(16.0, 7.0);
		p0.moveTo(6.0, 7.0);
		p0.lineTo(18.0, 7.0);
		p0.lineTo(18.0, 12.0);
		p0.moveTo(6.0, 12.0);
		p0.curveTo(6.174065, 14.281097, 7.63016, 16.2647, 9.754263, 17.114342);
		p0.curveTo(11.878366, 17.963983, 14.300782, 17.531784, 16.0, 16.0);
		p0.moveTo(12.0, 18.0);
		p0.lineTo(12.0, 22.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph83() {
		Shape p0 = new RoundRectangle2D.Double(3.0, 3.0, 18.0, 18.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(12.0, 3.0);
		p1.lineTo(12.0, 21.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph84() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(5.0, 3.0);
		p0.lineTo(5.0, 7.0);
		p0.curveTo(5.0, 12.0, 12.0, 10.0, 12.0, 15.0);
		p0.lineTo(12.0, 21.0);
		p0.moveTo(19.0, 3.0);
		p0.lineTo(19.0, 7.0);
		p0.curveTo(19.0, 12.0, 12.0, 10.0, 12.0, 15.0);
		p0.moveTo(8.0, 17.0);
		p0.lineTo(12.0, 21.0);
		p0.lineTo(16.0, 17.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph85() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(5.0, 3.0);
		p0.lineTo(5.0, 21.0);
		p0.moveTo(19.0, 3.0);
		p0.lineTo(19.0, 21.0);
		p0.moveTo(8.0, 8.0);
		p0.lineTo(16.0, 8.0);
		p0.moveTo(13.0, 5.0);
		p0.lineTo(16.0, 8.0);
		p0.lineTo(13.0, 11.0);
		p0.moveTo(16.0, 16.0);
		p0.lineTo(8.0, 16.0);
		p0.moveTo(11.0, 13.0);
		p0.lineTo(8.0, 16.0);
		p0.lineTo(11.0, 19.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph86() {
		Shape p0 = new Ellipse2D.Double(4.0, 2.0, 4.0, 4.0);
		Shape p1 = new Ellipse2D.Double(16.0, 2.0, 4.0, 4.0);
		Shape p2 = new Ellipse2D.Double(4.0, 18.0, 4.0, 4.0);
		Path2D.Double p3 = new Path2D.Double();
		p3.moveTo(6.0, 6.0);
		p3.lineTo(6.0, 18.0);
		p3.moveTo(18.0, 6.0);
		p3.lineTo(18.0, 9.0);
		p3.curveTo(18.0, 13.0, 6.0, 10.0, 6.0, 15.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null), new Part(p3, false, true, null)};
	}
	private static Part[] glyph87() {
		Shape p0 = new Ellipse2D.Double(3.0, 3.0, 18.0, 18.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(12.0, 6.0);
		p1.lineTo(12.0, 12.0);
		p1.lineTo(16.0, 15.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph88() {
		Shape p0 = new RoundRectangle2D.Double(3.0, 3.0, 18.0, 18.0, 4.0, 4.0);
		Shape p1 = new Ellipse2D.Double(6.0, 6.0, 4.0, 4.0);
		Path2D.Double p2 = new Path2D.Double();
		p2.moveTo(3.0, 17.0);
		p2.lineTo(8.0, 12.0);
		p2.lineTo(12.0, 16.0);
		p2.lineTo(16.0, 10.0);
		p2.lineTo(21.0, 17.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null)};
	}
	private static Part[] glyph89() {
		Shape p0 = new RoundRectangle2D.Double(2.0, 3.0, 20.0, 14.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(8.0, 21.0);
		p1.lineTo(16.0, 21.0);
		p1.moveTo(12.0, 17.0);
		p1.lineTo(12.0, 21.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph90() {
		Shape p0 = new RoundRectangle2D.Double(6.0, 2.0, 12.0, 20.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(10.0, 5.0);
		p1.lineTo(14.0, 5.0);
		p1.moveTo(11.0, 19.0);
		p1.lineTo(13.0, 19.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph91() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 9.0);
		p0.lineTo(7.0, 9.0);
		p0.lineTo(13.0, 4.0);
		p0.lineTo(13.0, 20.0);
		p0.lineTo(7.0, 15.0);
		p0.lineTo(3.0, 15.0);
		p0.lineTo(3.0, 9.0);
		p0.closePath();
		p0.moveTo(17.0, 8.0);
		p0.curveTo(19.037152, 10.277605, 19.037152, 13.722395, 17.0, 16.0);
		p0.moveTo(20.0, 5.0);
		p0.curveTo(23.811429, 8.888435, 23.811429, 15.111565, 20.0, 19.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph92() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 3.0);
		p0.lineTo(21.0, 3.0);
		p0.lineTo(21.0, 8.0);
		p0.lineTo(3.0, 8.0);
		p0.lineTo(3.0, 3.0);
		p0.closePath();
		p0.moveTo(5.0, 8.0);
		p0.lineTo(5.0, 21.0);
		p0.lineTo(19.0, 21.0);
		p0.lineTo(19.0, 8.0);
		p0.moveTo(10.0, 12.0);
		p0.lineTo(14.0, 12.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph93() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(4.0, 4.0);
		p0.lineTo(9.0, 4.0);
		p0.lineTo(9.0, 11.0);
		p0.lineTo(4.0, 11.0);
		p0.lineTo(4.0, 4.0);
		p0.closePath();
		p0.moveTo(15.0, 4.0);
		p0.lineTo(18.0, 4.0);
		p0.lineTo(18.0, 11.0);
		p0.moveTo(4.0, 15.0);
		p0.lineTo(7.0, 15.0);
		p0.lineTo(7.0, 22.0);
		p0.moveTo(14.0, 15.0);
		p0.lineTo(19.0, 15.0);
		p0.lineTo(19.0, 22.0);
		p0.lineTo(14.0, 22.0);
		p0.lineTo(14.0, 15.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph94() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 5.0);
		p0.lineTo(3.0, 19.0);
		p0.moveTo(10.0, 5.0);
		p0.lineTo(10.0, 19.0);
		p0.moveTo(3.0, 12.0);
		p0.lineTo(10.0, 12.0);
		p0.moveTo(16.0, 5.0);
		p0.lineTo(21.0, 5.0);
		p0.moveTo(16.0, 5.0);
		p0.lineTo(16.0, 19.0);
		p0.lineTo(21.0, 19.0);
		p0.moveTo(16.0, 12.0);
		p0.lineTo(20.0, 12.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph95() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 4.0);
		p0.lineTo(21.0, 4.0);
		p0.moveTo(12.0, 4.0);
		p0.lineTo(12.0, 21.0);
		p0.moveTo(8.0, 21.0);
		p0.lineTo(16.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph96() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(6.0, 16.0);
		p0.lineTo(6.01, 16.0);
		p0.moveTo(16.0, 3.0);
		p0.lineTo(16.0, 15.0);
		p0.moveTo(10.0, 6.0);
		p0.lineTo(22.0, 12.0);
		p0.moveTo(10.0, 12.0);
		p0.lineTo(22.0, 6.0);
		Shape p1 = new Ellipse2D.Double(5.0, 16.0, 2.0, 2.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, true, true, null)};
	}
	private static Part[] glyph97() {
		Shape p0 = new Ellipse2D.Double(3.0, 3.0, 10.0, 10.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(12.0, 12.0);
		p1.lineTo(21.0, 21.0);
		p1.moveTo(18.0, 18.0);
		p1.lineTo(21.0, 15.0);
		p1.moveTo(15.0, 15.0);
		p1.lineTo(18.0, 12.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph98() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 3.0);
		p0.lineTo(15.0, 9.0);
		p0.lineTo(22.0, 10.0);
		p0.lineTo(17.0, 15.0);
		p0.lineTo(18.0, 22.0);
		p0.lineTo(12.0, 18.0);
		p0.lineTo(6.0, 22.0);
		p0.lineTo(7.0, 15.0);
		p0.lineTo(2.0, 10.0);
		p0.lineTo(9.0, 9.0);
		p0.lineTo(12.0, 3.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph99() {
		Shape p0 = new Ellipse2D.Double(8.0, 3.0, 8.0, 8.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(4.0, 21.0);
		p1.lineTo(4.0, 18.0);
		p1.curveTo(4.0, 13.581722, 7.581722, 10.0, 12.0, 10.0);
		p1.curveTo(16.418278, 10.0, 20.0, 13.581722, 20.0, 18.0);
		p1.lineTo(20.0, 21.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph100() {
		Shape p0 = new Ellipse2D.Double(6.0, 4.0, 6.0, 6.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(3.0, 21.0);
		p1.lineTo(3.0, 17.0);
		p1.curveTo(3.0, 13.686292, 5.686292, 11.0, 9.0, 11.0);
		p1.curveTo(12.313708, 11.0, 15.0, 13.686292, 15.0, 17.0);
		p1.lineTo(15.0, 21.0);
		p1.moveTo(17.0, 4.0);
		p1.curveTo(18.656854, 4.0, 20.0, 5.343146, 20.0, 7.0);
		p1.curveTo(20.0, 8.656854, 18.656854, 10.0, 17.0, 10.0);
		p1.moveTo(18.0, 14.0);
		p1.curveTo(20.365616, 14.482368, 22.048679, 16.586196, 22.0, 19.0);
		p1.lineTo(22.0, 21.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph101() {
		Shape p0 = new Ellipse2D.Double(3.0, 3.0, 18.0, 18.0);
		Shape p1 = new Ellipse2D.Double(7.0, 7.0, 10.0, 10.0);
		Path2D.Double p2 = new Path2D.Double();
		p2.moveTo(12.0, 2.0);
		p2.lineTo(12.0, 6.0);
		p2.moveTo(12.0, 18.0);
		p2.lineTo(12.0, 22.0);
		p2.moveTo(2.0, 12.0);
		p2.lineTo(6.0, 12.0);
		p2.moveTo(18.0, 12.0);
		p2.lineTo(22.0, 12.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null)};
	}
	private static Part[] glyph102() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(2.0, 12.0);
		p0.curveTo(7.0, 2.0, 17.0, 2.0, 22.0, 12.0);
		p0.curveTo(17.0, 22.0, 7.0, 22.0, 2.0, 12.0);
		p0.closePath();
		Shape p1 = new Ellipse2D.Double(9.0, 9.0, 6.0, 6.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph103() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 2.0);
		p0.lineTo(12.0, 22.0);
		p0.moveTo(2.0, 12.0);
		p0.lineTo(22.0, 12.0);
		p0.moveTo(8.0, 6.0);
		p0.lineTo(12.0, 2.0);
		p0.lineTo(16.0, 6.0);
		p0.moveTo(8.0, 18.0);
		p0.lineTo(12.0, 22.0);
		p0.lineTo(16.0, 18.0);
		p0.moveTo(6.0, 8.0);
		p0.lineTo(2.0, 12.0);
		p0.lineTo(6.0, 16.0);
		p0.moveTo(18.0, 8.0);
		p0.lineTo(22.0, 12.0);
		p0.lineTo(18.0, 16.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph104() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(4.0, 3.0);
		p0.lineTo(8.0, 20.0);
		p0.lineTo(12.0, 14.0);
		p0.lineTo(19.0, 11.0);
		p0.lineTo(4.0, 3.0);
		p0.closePath();
		p0.moveTo(12.0, 14.0);
		p0.lineTo(18.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph105() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 5.0);
		p0.lineTo(21.0, 5.0);
		p0.moveTo(8.0, 12.0);
		p0.lineTo(21.0, 12.0);
		p0.moveTo(8.0, 19.0);
		p0.lineTo(21.0, 19.0);
		p0.moveTo(3.0, 5.0);
		p0.lineTo(3.01, 5.0);
		p0.moveTo(3.0, 12.0);
		p0.lineTo(3.01, 12.0);
		p0.moveTo(3.0, 19.0);
		p0.lineTo(3.01, 19.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph106() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 17.0);
		p0.curveTo(8.0, 14.0, 4.0, 14.0, 4.0, 9.0);
		p0.curveTo(4.0, 4.581722, 7.581722, 1.0, 12.0, 1.0);
		p0.curveTo(16.418278, 1.0, 20.0, 4.581722, 20.0, 9.0);
		p0.curveTo(20.0, 14.0, 16.0, 14.0, 16.0, 17.0);
		p0.lineTo(8.0, 17.0);
		p0.closePath();
		p0.moveTo(9.0, 21.0);
		p0.lineTo(15.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph107() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(2.0, 12.0);
		p0.lineTo(5.0, 12.0);
		p0.lineTo(8.0, 4.0);
		p0.lineTo(13.0, 20.0);
		p0.lineTo(16.0, 12.0);
		p0.lineTo(22.0, 12.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph108() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 3.0);
		p0.lineTo(12.0, 21.0);
		p0.moveTo(4.0, 7.0);
		p0.lineTo(20.0, 17.0);
		p0.moveTo(4.0, 17.0);
		p0.lineTo(20.0, 7.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph109() {
		Shape p0 = new Ellipse2D.Double(4.0, 4.0, 16.0, 16.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph110() {
		Shape p0 = new Ellipse2D.Double(3.0, 3.0, 18.0, 18.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(7.0, 12.0);
		p1.lineTo(10.0, 15.0);
		p1.lineTo(17.0, 8.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph111() {
		Shape p0 = new Ellipse2D.Double(3.0, 3.0, 18.0, 18.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(7.0, 12.0);
		p1.lineTo(17.0, 12.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph112() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(5.0, 3.0);
		p0.lineTo(5.0, 17.0);
		p0.lineTo(20.0, 17.0);
		p0.moveTo(5.0, 8.0);
		p0.lineTo(20.0, 8.0);
		p0.moveTo(12.0, 8.0);
		p0.lineTo(12.0, 17.0);
		Shape p1 = new RoundRectangle2D.Double(17.0, 5.0, 5.0, 5.0, 2.0, 2.0);
		Shape p2 = new RoundRectangle2D.Double(17.0, 14.0, 5.0, 5.0, 2.0, 2.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null)};
	}
	private static Part[] glyph113() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 7.0);
		p0.lineTo(21.0, 7.0);
		p0.moveTo(17.0, 3.0);
		p0.lineTo(21.0, 7.0);
		p0.lineTo(17.0, 11.0);
		p0.moveTo(21.0, 17.0);
		p0.lineTo(3.0, 17.0);
		p0.moveTo(7.0, 13.0);
		p0.lineTo(3.0, 17.0);
		p0.lineTo(7.0, 21.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph114() {
		Shape p0 = new Ellipse2D.Double(4.0, 18.0, 2.0, 2.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(4.0, 11.0);
		p1.curveTo(8.970563, 11.0, 13.0, 15.029437, 13.0, 20.0);
		p1.moveTo(4.0, 3.0);
		p1.curveTo(13.388841, 3.0, 21.0, 10.611159, 21.0, 20.0);
		return new Part[]{new Part(p0, true, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph115() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 20.0);
		p0.lineTo(12.0, 3.0);
		p0.moveTo(9.0, 6.0);
		p0.lineTo(12.0, 3.0);
		p0.lineTo(15.0, 6.0);
		p0.moveTo(12.0, 15.0);
		p0.curveTo(6.0, 15.0, 6.0, 12.0, 6.0, 9.0);
		p0.moveTo(12.0, 12.0);
		p0.curveTo(18.0, 12.0, 18.0, 9.0, 18.0, 7.0);
		Shape p1 = new Ellipse2D.Double(10.0, 18.0, 4.0, 4.0);
		Shape p2 = new Ellipse2D.Double(5.0, 7.0, 2.0, 2.0);
		Path2D.Double p3 = new Path2D.Double();
		p3.moveTo(17.0, 4.0);
		p3.lineTo(19.0, 4.0);
		p3.lineTo(19.0, 6.0);
		p3.lineTo(17.0, 6.0);
		p3.lineTo(17.0, 4.0);
		p3.closePath();
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null), new Part(p3, false, true, null)};
	}
	private static Part[] glyph116() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 9.0);
		p0.lineTo(3.0, 3.0);
		p0.lineTo(9.0, 3.0);
		p0.moveTo(15.0, 3.0);
		p0.lineTo(21.0, 3.0);
		p0.lineTo(21.0, 9.0);
		p0.moveTo(21.0, 15.0);
		p0.lineTo(21.0, 21.0);
		p0.lineTo(15.0, 21.0);
		p0.moveTo(9.0, 21.0);
		p0.lineTo(3.0, 21.0);
		p0.lineTo(3.0, 15.0);
		p0.moveTo(8.0, 8.0);
		p0.lineTo(16.0, 8.0);
		p0.lineTo(16.0, 16.0);
		p0.lineTo(8.0, 16.0);
		p0.lineTo(8.0, 8.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph117() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(5.0, 9.0);
		p0.lineTo(12.0, 16.0);
		p0.lineTo(19.0, 9.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph118() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(5.0, 17.0);
		p0.lineTo(5.0, 5.0);
		p0.lineTo(17.0, 5.0);
		p0.moveTo(5.0, 5.0);
		p0.lineTo(19.0, 19.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph119() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(19.0, 7.0);
		p0.lineTo(19.0, 19.0);
		p0.lineTo(7.0, 19.0);
		p0.moveTo(19.0, 19.0);
		p0.lineTo(5.0, 5.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph120() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 3.0);
		p0.curveTo(7.029437, 3.0, 3.0, 7.029437, 3.0, 12.0);
		p0.curveTo(3.0, 16.970563, 7.029437, 21.0, 12.0, 21.0);
		p0.lineTo(13.0, 21.0);
		p0.curveTo(14.104569, 21.0, 15.0, 20.104569, 15.0, 19.0);
		p0.curveTo(15.0, 17.895431, 14.104569, 17.0, 13.0, 17.0);
		p0.lineTo(12.0, 17.0);
		p0.curveTo(10.895431, 17.0, 10.0, 16.104569, 10.0, 15.0);
		p0.curveTo(10.0, 13.895431, 10.895431, 13.0, 12.0, 13.0);
		p0.lineTo(16.0, 13.0);
		p0.curveTo(18.761424, 13.0, 21.0, 10.761424, 21.0, 8.0);
		p0.curveTo(21.0, 5.238576, 18.761424, 3.0, 16.0, 3.0);
		p0.lineTo(12.0, 3.0);
		p0.closePath();
		Shape p1 = new Ellipse2D.Double(6.0, 10.0, 2.0, 2.0);
		Shape p2 = new Ellipse2D.Double(9.0, 6.0, 2.0, 2.0);
		Shape p3 = new Ellipse2D.Double(14.0, 6.0, 2.0, 2.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null), new Part(p3, false, true, null)};
	}
	private static Part[] glyph121() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(14.0, 3.0);
		p0.lineTo(18.0, 7.0);
		p0.lineTo(10.0, 15.0);
		p0.lineTo(6.0, 11.0);
		p0.lineTo(14.0, 3.0);
		p0.closePath();
		p0.moveTo(6.0, 11.0);
		p0.lineTo(3.0, 18.0);
		p0.lineTo(6.0, 21.0);
		p0.lineTo(13.0, 18.0);
		p0.lineTo(10.0, 15.0);
		p0.moveTo(5.0, 16.0);
		p0.lineTo(8.0, 19.0);
		p0.moveTo(16.0, 14.0);
		p0.lineTo(21.0, 14.0);
		p0.moveTo(17.0, 18.0);
		p0.lineTo(21.0, 18.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph122() {
		Shape p0 = new RoundRectangle2D.Double(3.0, 3.0, 18.0, 18.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(12.0, 7.0);
		p1.lineTo(12.0, 11.0);
		p1.moveTo(7.0, 15.0);
		p1.lineTo(12.0, 11.0);
		p1.lineTo(17.0, 15.0);
		Shape p2 = new Ellipse2D.Double(11.0, 6.0, 2.0, 2.0);
		Shape p3 = new Ellipse2D.Double(5.0, 14.0, 4.0, 4.0);
		Shape p4 = new Ellipse2D.Double(15.0, 14.0, 4.0, 4.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null), new Part(p3, false, true, null), new Part(p4, false, true, null)};
	}
	private static Part[] glyph123() {
		Shape p0 = new RoundRectangle2D.Double(3.0, 3.0, 18.0, 18.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(8.0, 3.0);
		p1.lineTo(8.0, 21.0);
		p1.moveTo(12.0, 7.0);
		p1.lineTo(17.0, 7.0);
		p1.moveTo(12.0, 12.0);
		p1.lineTo(17.0, 12.0);
		p1.moveTo(12.0, 17.0);
		p1.lineTo(15.0, 17.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph124() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(7.0, 4.0);
		p0.lineTo(5.0, 4.0);
		p0.lineTo(5.0, 10.0);
		p0.lineTo(3.0, 12.0);
		p0.lineTo(5.0, 14.0);
		p0.lineTo(5.0, 20.0);
		p0.lineTo(7.0, 20.0);
		p0.moveTo(17.0, 4.0);
		p0.lineTo(19.0, 4.0);
		p0.lineTo(19.0, 10.0);
		p0.lineTo(21.0, 12.0);
		p0.lineTo(19.0, 14.0);
		p0.lineTo(19.0, 20.0);
		p0.lineTo(17.0, 20.0);
		p0.moveTo(9.0, 12.0);
		p0.lineTo(15.0, 12.0);
		p0.moveTo(12.0, 9.0);
		p0.lineTo(15.0, 12.0);
		p0.lineTo(12.0, 15.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph125() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 7.0);
		p0.lineTo(8.0, 12.0);
		p0.lineTo(3.0, 17.0);
		p0.moveTo(12.0, 5.0);
		p0.lineTo(21.0, 5.0);
		p0.moveTo(12.0, 10.0);
		p0.lineTo(18.0, 10.0);
		p0.moveTo(12.0, 15.0);
		p0.lineTo(21.0, 15.0);
		p0.moveTo(12.0, 20.0);
		p0.lineTo(18.0, 20.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph126() {
		Shape p0 = new RoundRectangle2D.Double(3.0, 4.0, 9.0, 16.0, 2.0, 2.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(18.0, 4.0);
		p1.lineTo(18.0, 20.0);
		p1.moveTo(15.0, 17.0);
		p1.lineTo(18.0, 20.0);
		p1.lineTo(21.0, 17.0);
		return new Part[]{new Part(p0, false, true, new float[]{3.0f,3.0f}), new Part(p1, false, true, null)};
	}
	private static Part[] glyph127() {
		Shape p0 = new RoundRectangle2D.Double(3.0, 4.0, 9.0, 16.0, 2.0, 2.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(18.0, 20.0);
		p1.lineTo(18.0, 4.0);
		p1.moveTo(15.0, 7.0);
		p1.lineTo(18.0, 4.0);
		p1.lineTo(21.0, 7.0);
		return new Part[]{new Part(p0, false, true, new float[]{3.0f,3.0f}), new Part(p1, false, true, null)};
	}
	private static Part[] glyph128() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 13.0);
		p0.lineTo(9.0, 4.0);
		p0.lineTo(14.0, 8.0);
		p0.lineTo(8.0, 17.0);
		p0.lineTo(3.0, 17.0);
		p0.lineTo(3.0, 13.0);
		p0.closePath();
		p0.moveTo(3.0, 21.0);
		p0.lineTo(11.0, 21.0);
		p0.moveTo(19.0, 4.0);
		p0.lineTo(19.0, 20.0);
		p0.moveTo(16.0, 17.0);
		p0.lineTo(19.0, 20.0);
		p0.lineTo(22.0, 17.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph129() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 13.0);
		p0.lineTo(9.0, 4.0);
		p0.lineTo(14.0, 8.0);
		p0.lineTo(8.0, 17.0);
		p0.lineTo(3.0, 17.0);
		p0.lineTo(3.0, 13.0);
		p0.closePath();
		p0.moveTo(3.0, 21.0);
		p0.lineTo(11.0, 21.0);
		p0.moveTo(19.0, 20.0);
		p0.lineTo(19.0, 4.0);
		p0.moveTo(16.0, 7.0);
		p0.lineTo(19.0, 4.0);
		p0.lineTo(22.0, 7.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph130() {
		Shape p0 = new RoundRectangle2D.Double(4.0, 3.0, 16.0, 18.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(4.0, 9.0);
		p1.lineTo(20.0, 9.0);
		p1.moveTo(4.0, 15.0);
		p1.lineTo(20.0, 15.0);
		p1.moveTo(8.0, 6.0);
		p1.lineTo(9.0, 6.0);
		p1.moveTo(8.0, 12.0);
		p1.lineTo(9.0, 12.0);
		p1.moveTo(8.0, 18.0);
		p1.lineTo(9.0, 18.0);
		p1.moveTo(14.0, 6.0);
		p1.lineTo(16.0, 6.0);
		p1.moveTo(14.0, 12.0);
		p1.lineTo(16.0, 12.0);
		p1.moveTo(14.0, 18.0);
		p1.lineTo(16.0, 18.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph131() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 4.0);
		p0.lineTo(13.0, 4.0);
		p0.moveTo(3.0, 9.0);
		p0.lineTo(13.0, 9.0);
		p0.moveTo(3.0, 14.0);
		p0.lineTo(9.0, 14.0);
		p0.moveTo(3.0, 19.0);
		p0.lineTo(9.0, 19.0);
		p0.moveTo(18.0, 8.0);
		p0.lineTo(18.0, 20.0);
		p0.moveTo(14.0, 16.0);
		p0.lineTo(18.0, 20.0);
		p0.lineTo(22.0, 16.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph132() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 5.0);
		p0.curveTo(9.0, 3.0, 5.0, 3.0, 2.0, 4.0);
		p0.lineTo(2.0, 19.0);
		p0.curveTo(5.0, 18.0, 9.0, 18.0, 12.0, 20.0);
		p0.curveTo(15.0, 18.0, 19.0, 18.0, 22.0, 19.0);
		p0.lineTo(22.0, 4.0);
		p0.curveTo(19.0, 3.0, 15.0, 3.0, 12.0, 5.0);
		p0.closePath();
		p0.moveTo(12.0, 5.0);
		p0.lineTo(12.0, 20.0);
		p0.moveTo(7.0, 8.0);
		p0.lineTo(5.0, 11.0);
		p0.lineTo(7.0, 14.0);
		p0.moveTo(17.0, 8.0);
		p0.lineTo(19.0, 11.0);
		p0.lineTo(17.0, 14.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph133() {
		Shape p0 = new RoundRectangle2D.Double(3.0, 3.0, 18.0, 18.0, 4.0, 4.0);
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(3.0, 8.0);
		p1.lineTo(21.0, 8.0);
		p1.moveTo(8.0, 8.0);
		p1.lineTo(8.0, 21.0);
		p1.moveTo(12.0, 12.0);
		p1.lineTo(10.0, 14.0);
		p1.lineTo(12.0, 16.0);
		p1.moveTo(17.0, 12.0);
		p1.lineTo(19.0, 14.0);
		p1.lineTo(17.0, 16.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph134() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(12.0, 3.0);
		p0.lineTo(5.0, 3.0);
		p0.curveTo(3.895431, 3.0, 3.0, 3.895431, 3.0, 5.0);
		p0.lineTo(3.0, 19.0);
		p0.curveTo(3.0, 20.104569, 3.895431, 21.0, 5.0, 21.0);
		p0.lineTo(19.0, 21.0);
		p0.curveTo(20.104569, 21.0, 21.0, 20.104569, 21.0, 19.0);
		p0.lineTo(21.0, 12.0);
		p0.moveTo(15.0, 3.0);
		p0.lineTo(21.0, 3.0);
		p0.lineTo(21.0, 9.0);
		p0.moveTo(21.0, 3.0);
		p0.lineTo(12.0, 12.0);
		p0.moveTo(7.0, 13.0);
		p0.lineTo(10.0, 16.0);
		p0.lineTo(7.0, 19.0);
		p0.moveTo(12.0, 18.0);
		p0.lineTo(16.0, 18.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph135() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(11.0, 3.0);
		p0.lineTo(17.0, 3.0);
		p0.moveTo(14.0, 3.0);
		p0.lineTo(14.0, 21.0);
		p0.moveTo(11.0, 21.0);
		p0.lineTo(17.0, 21.0);
		p0.moveTo(10.0, 7.0);
		p0.lineTo(3.0, 7.0);
		p0.lineTo(3.0, 17.0);
		p0.lineTo(10.0, 17.0);
		p0.moveTo(18.0, 7.0);
		p0.lineTo(21.0, 7.0);
		p0.lineTo(21.0, 17.0);
		p0.lineTo(18.0, 17.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph136() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(10.0, 8.0);
		p0.lineTo(16.0, 12.0);
		p0.lineTo(10.0, 16.0);
		p0.lineTo(10.0, 8.0);
		p0.closePath();
		p0.moveTo(20.0, 7.0);
		p0.curveTo(22.259866, 10.396281, 21.95275, 14.887829, 19.251628, 17.944896);
		p0.curveTo(16.550506, 21.001962, 12.130978, 21.859919, 8.482228, 20.035544);
		p0.curveTo(4.833478, 18.21117, 2.868127, 14.160773, 3.693107, 10.165635);
		p0.curveTo(4.518087, 6.170497, 7.927056, 3.229876, 12.0, 3.0);
		p0.moveTo(16.0, 3.0);
		p0.lineTo(21.0, 3.0);
		p0.lineTo(21.0, 8.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph137() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 4.0);
		p0.lineTo(21.0, 4.0);
		p0.lineTo(14.0, 12.0);
		p0.lineTo(14.0, 15.0);
		p0.moveTo(10.0, 12.0);
		p0.lineTo(10.0, 19.0);
		p0.lineTo(14.0, 21.0);
		p0.moveTo(17.0, 17.0);
		p0.lineTo(22.0, 22.0);
		p0.moveTo(17.0, 22.0);
		p0.lineTo(22.0, 17.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph138() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 3.0);
		p0.lineTo(3.0, 16.0);
		p0.lineTo(8.0, 16.0);
		p0.moveTo(3.0, 8.0);
		p0.lineTo(8.0, 8.0);
		p0.moveTo(11.0, 4.0);
		p0.lineTo(21.0, 4.0);
		p0.lineTo(18.0, 9.0);
		p0.lineTo(18.0, 18.0);
		p0.lineTo(14.0, 21.0);
		p0.lineTo(14.0, 9.0);
		p0.lineTo(11.0, 4.0);
		p0.closePath();
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph139() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 3.0);
		p0.lineTo(3.0, 16.0);
		p0.lineTo(8.0, 16.0);
		p0.moveTo(3.0, 8.0);
		p0.lineTo(8.0, 8.0);
		p0.moveTo(12.0, 8.0);
		p0.lineTo(21.0, 8.0);
		p0.moveTo(12.0, 14.0);
		p0.lineTo(21.0, 14.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph140() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 3.0);
		p0.lineTo(3.0, 16.0);
		p0.lineTo(8.0, 16.0);
		p0.moveTo(3.0, 8.0);
		p0.lineTo(8.0, 8.0);
		p0.moveTo(12.0, 5.0);
		p0.lineTo(12.0, 19.0);
		p0.moveTo(16.0, 9.0);
		p0.lineTo(21.0, 9.0);
		p0.moveTo(16.0, 15.0);
		p0.lineTo(19.0, 15.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph141() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(3.0, 3.0);
		p0.lineTo(3.0, 16.0);
		p0.lineTo(8.0, 16.0);
		p0.moveTo(3.0, 8.0);
		p0.lineTo(8.0, 8.0);
		p0.moveTo(16.0, 5.0);
		p0.lineTo(16.0, 15.0);
		p0.moveTo(12.0, 8.0);
		p0.lineTo(20.0, 12.0);
		p0.moveTo(20.0, 8.0);
		p0.lineTo(12.0, 12.0);
		Shape p1 = new Ellipse2D.Double(19.0, 18.0, 2.0, 2.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null)};
	}
	private static Part[] glyph142() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(13.0, 4.0);
		p0.lineTo(4.0, 4.0);
		p0.lineTo(4.0, 21.0);
		p0.lineTo(9.0, 17.0);
		p0.lineTo(13.0, 21.0);
		p0.lineTo(13.0, 4.0);
		p0.closePath();
		p0.moveTo(19.0, 4.0);
		p0.lineTo(19.0, 20.0);
		p0.moveTo(16.0, 17.0);
		p0.lineTo(19.0, 20.0);
		p0.lineTo(22.0, 17.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
	private static Part[] glyph143() {
		Shape p0 = new Ellipse2D.Double(3.0, 4.0, 4.0, 4.0);
		Shape p1 = new Ellipse2D.Double(16.0, 4.0, 4.0, 4.0);
		Shape p2 = new Ellipse2D.Double(10.0, 17.0, 4.0, 4.0);
		Path2D.Double p3 = new Path2D.Double();
		p3.moveTo(8.0, 6.0);
		p3.lineTo(14.0, 6.0);
		p3.moveTo(12.0, 4.0);
		p3.lineTo(14.0, 6.0);
		p3.lineTo(12.0, 8.0);
		p3.moveTo(17.0, 9.0);
		p3.lineTo(14.0, 15.0);
		p3.moveTo(17.0, 14.0);
		p3.lineTo(14.0, 15.0);
		p3.lineTo(13.0, 12.0);
		p3.moveTo(9.0, 18.0);
		p3.lineTo(5.0, 10.0);
		p3.moveTo(4.0, 13.0);
		p3.lineTo(5.0, 10.0);
		p3.lineTo(8.0, 11.0);
		return new Part[]{new Part(p0, false, true, null), new Part(p1, false, true, null), new Part(p2, false, true, null), new Part(p3, false, true, null)};
	}
	private static Part[] glyph144() {
		Path2D.Double p0 = new Path2D.Double();
		p0.moveTo(8.0, 3.0);
		p0.lineTo(3.0, 3.0);
		p0.lineTo(3.0, 21.0);
		p0.lineTo(8.0, 21.0);
		p0.moveTo(16.0, 3.0);
		p0.lineTo(21.0, 3.0);
		p0.lineTo(21.0, 21.0);
		p0.lineTo(16.0, 21.0);
		p0.moveTo(8.0, 9.0);
		p0.lineTo(16.0, 9.0);
		p0.moveTo(8.0, 15.0);
		p0.lineTo(16.0, 15.0);
		return new Part[]{new Part(p0, false, true, null)};
	}
}
