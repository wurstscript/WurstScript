package de.peeeq.wurstio.gui;

import java.awt.Rectangle;
import java.awt.Window;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class GuiUtils {

	public static void setWindowToCenterOfScreen(Window frm) {
		Rectangle screenBounds = frm.getGraphicsConfiguration().getBounds();

		int center_x = screenBounds.x + screenBounds.width / 2;
		int center_y = screenBounds.y + screenBounds.height / 2;

		frm.setLocation(center_x - frm.getWidth() / 2,
				center_y - frm.getHeight() / 2);
	}

	public static <K, V> Map<V, K> filterByType(Class<? extends K> type,
			Map<V, ?> map) {
		Map<V, K> result = Maps.newHashMap();
		for (Entry<V, ?> e : map.entrySet()) {
			if (type.isInstance(e.getValue())) {
				result.put(e.getKey(), (K) e.getValue());
			}
		}
		return result;
	}

}
