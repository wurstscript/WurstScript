package de.peeeq.eclipsewurstplugin.editor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.utils.Utils;

public class WurstInformationControl extends DefaultInformationControl {

	@SuppressWarnings("deprecation") // using deprecated constructor because other constructors do not allow WRAP
	public WurstInformationControl(Shell parent) {
		super(parent, SWT.WRAP, new WurstInformationPresenter(), "Press 'F2' for focus.");
	}
	
	private static class WurstInformationPresenter implements IInformationPresenter {
		private Map<String, NodeHandler> nodeHandlers = Maps.newHashMap();
		private Display display;
		
		abstract class NodeHandler {
			Element element;
			void before(List<StyleRangeCustom> styles, StringBuilder sb, int offset) {	
			}
			abstract void after(List<StyleRangeCustom> styles, StringBuilder sb, int stop, int length);
		}
		
		class StyleRangeCustom {
			final int start;
			final int stop;
			final List<StyleRangeModifier> styles;
			
			public StyleRangeCustom(int start, int stop, StyleRangeModifier ... styles) {
				this.start = start;
				this.stop = stop;
				this.styles = Lists.newArrayList(styles);
			}

			public void style(StyleRange range) {
				for (StyleRangeModifier style : styles) {
					style.modify(range);
				}
			}
			
			@Override
			public String toString() {
				return "from " + start + " to " + stop + ": " + Utils.join(styles, ", ");
			}
			
		}
		
		abstract class StyleRangeModifier {
			abstract void modify(StyleRange r);
		}
		
		class StyleRangeModifierFont extends StyleRangeModifier {

			private Font font;

			public StyleRangeModifierFont(Font font) {
				this.font =  font;
			}
			
			@Override
			void modify(StyleRange r) {
				r.font = font;
			}
			
			@Override
			public String toString() {
				return "font " + font;
			}
			
		}
		
		class StyleRangeModifierFontStyle extends StyleRangeModifier {
			private int fontstyle;

			public StyleRangeModifierFontStyle(int fontstyle) {
				this.fontstyle = fontstyle;
			}
			
			@Override
			void modify(StyleRange r) {
				r.fontStyle |= fontstyle;
				
			}
			
			@Override
			public String toString() {
				return "fontstyle " + fontstyle;
			}
			
		}
		
		class StyleRangeModifierColor extends StyleRangeModifier {

			private Color color;

			public StyleRangeModifierColor(Color color) {
				this.color = color;
			}
			
			@Override
			void modify(StyleRange r) {
				r.foreground = color;
			}
			
			@Override
			public String toString() {
				return "color " + color;
			}
			
		}
		
		@Override
		public String updatePresentation(Display display, String hoverInfo,
				TextPresentation presentation, int maxWidth, int maxHeight) {
			this.display = display;
//			StyleRange range = new StyleRange(2, 4, null, null);
//			FontRegistry reg = new FontRegistry(display);
//			System.out.println("fonts = " + reg.getKeySet());
//			range.font = new Font(display, "Courier New", 13, 0);//reg.get("serif");
//			System.out.println("font = " + range.font);
//			presentation.addStyleRange(range);
			System.out.println(hoverInfo);
			
			
			StringBuilder sb = new StringBuilder();
			try {
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = builderFactory.newDocumentBuilder();
				Document doc = builder.parse(
						new InputSource(new StringReader("<root>" + hoverInfo + "</root>")));
				parseDocument(doc, presentation, sb);
			} catch (Exception e) {
				e.printStackTrace();
				return hoverInfo;
			}
			Iterator<StyleRange> it = presentation.getAllStyleRangeIterator();
			while (it.hasNext()) {
				StyleRange range = it.next();
				System.out.println("range = " + range);
				
			}
			
			
			return sb.toString();
		}

		private void parseDocument(Document doc, TextPresentation presentation,	StringBuilder sb) {
			initNodeHandlers();
			
			List<StyleRangeCustom> styles = Lists.newArrayList();
			parseElement(doc.getDocumentElement(), styles, sb);
			
			for (StyleRangeCustom style : styles) {
				System.out.println("style " + style);
				System.out.println("	" + sb.substring(style.start, style.stop));
			}
			
			Set<Integer> changepoints = getChangePoints(styles);
			List<Integer> changepointsSorted = Lists.newArrayList(changepoints);
			Collections.sort(changepointsSorted);
			for (int point : changepointsSorted) {
				makeChange(presentation, point, styles, sb);
			}
		}



		private void makeChange(TextPresentation presentation, int point, List<StyleRangeCustom> styles, StringBuilder sb) {
			List<StyleRangeCustom> activeStyles = Lists.newArrayList();
			int end = Integer.MAX_VALUE;
			for (StyleRangeCustom s : styles) {
				if (s.start <= point) {
					if (s.stop > point) {
						activeStyles.add(s);
						end = Math.min(end, s.stop);
					}
				} else {
					end = Math.min(end, s.start);
				}
			}
			if (activeStyles.isEmpty()) {
				return;
			}
			StyleRange range = new StyleRange(point, end - point, null, null);
			for (StyleRangeCustom r : activeStyles) {
				r.style(range);
			}
			System.out.println("adding range from " + point + " to " + end);
			System.out.println("	" + range);
			System.out.println("	" + sb.substring(point, end));
			presentation.addStyleRange(range);
			
		}

		private Set<Integer> getChangePoints(List<StyleRangeCustom> styles) {
			Set<Integer> result = Sets.newHashSet();
			for (StyleRangeCustom style : styles) {
				result.add(style.start);
				result.add(style.stop);
			}
			return result;
		}

		private void initNodeHandlers() {
			nodeHandlers.clear();
			nodeHandlers.put("b", new NodeHandler() {
				
				@Override
				public void after(List<StyleRangeCustom> styles, StringBuilder sb,
						int offset, int length) {
					styles.add(new StyleRangeCustom(offset, length, new StyleRangeModifierFontStyle(SWT.BOLD)));
					
				}
			});
			
			nodeHandlers.put("i", new NodeHandler() {
				
				@Override
				public void after(List<StyleRangeCustom> styles, StringBuilder sb,
						int offset, int length) {
					styles.add(new StyleRangeCustom(offset, length, new StyleRangeModifierFontStyle(SWT.ITALIC)));
				}
			});
			
			nodeHandlers.put("font", new NodeHandler() {
				
				@Override
				public void after(List<StyleRangeCustom> styles, StringBuilder sb,
						int offset, int length) {
					String colorstring = this.element.getAttribute("color");
					Pattern p = Pattern.compile("rgb\\(([0-9]+),([0-9]+),([0-9]+)\\)");
					Matcher m = p.matcher(colorstring);
					if (m.find()) {
					    int red = Integer.parseInt(m.group(1));
					    int green = Integer.parseInt(m.group(2));
					    int blue = Integer.parseInt(m.group(3));
					    Color color = new Color(display, red, green, blue);
						styles.add(new StyleRangeCustom(offset, length, new StyleRangeModifierColor(color)));
					}
					
				}
			});
			
			nodeHandlers.put("hr", new NodeHandler() {
				
				@Override
				public void after(List<StyleRangeCustom> styles, StringBuilder sb,
						int stop, int length) {
					sb.append("\n---\n");
				}
			});
			nodeHandlers.put("br", new NodeHandler() {
				
				@Override
				public void after(List<StyleRangeCustom> styles, StringBuilder sb,
						int stop, int length) {
					sb.append("\n");
				}
			});
			
			nodeHandlers.put("pre", new NodeHandler() {
				
				@Override
				void before(List<StyleRangeCustom> styles, StringBuilder sb,
						int offset) {
					sb.append("\n");
				}
				
				@Override
				public void after(List<StyleRangeCustom> styles, StringBuilder sb,
						int stop, int length) {
					sb.append("\n");
					StyleRangeCustom range = new StyleRangeCustom(stop, length
							, new StyleRangeModifierFont(new Font(display, "Courier New", 11, 0)));
					styles.add(range);
					
				}
			});
		}

		private void parseElement(Element e,
				List<StyleRangeCustom> styles, StringBuilder sb) {
			NodeHandler nh = nodeHandlers.get(e.getNodeName());
			int offset = sb.length();
			if (nh == null) {
				System.out.println("unhandled tag " + e.getNodeName());
			} else {
				nh.element = e;
				nh.before(styles, sb, offset);
			}
			NodeList children = e.getChildNodes();
			for (int i=0; i<children.getLength(); i++) { 
				parseNode(children.item(i), styles, sb);
			}
			if (nh != null) {
				nh.element = e;
				nh.after(styles, sb, offset, sb.length());
			}
		}

		private void parseNode(Node item, List<StyleRangeCustom> styles,
				StringBuilder sb) {
			if (item instanceof Element) {
				parseElement((Element) item, styles, sb);
			} else if (item instanceof Text) {
				Text text = (Text) item;
				sb.append(text.getWholeText());
			} else {
				System.out.println("unhandled case " + item.getClass() + "\n\t" + item);
			}
			
		}
		
	}

	
	
}
