package eu.unipi.fidouafsvc.model;

/**
 * Created by georgeg on 01.07.2016.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * This class contains information about each module.
 */

public class About {

	public HashMap<String, String> about;

	public About(Manifest manifest) {
		about = new HashMap();
		for (Map.Entry entry : manifest.getMainAttributes().entrySet()) {
			about.put(entry.getKey().toString(), entry.getValue().toString());
		}
	}
}
