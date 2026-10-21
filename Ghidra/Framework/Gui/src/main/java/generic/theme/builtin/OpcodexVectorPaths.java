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
}
