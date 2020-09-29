/*
 * stiched together with the help of 
 * http://hg.openjdk.java.net/jdk9/jdk9/jdk/rev/78a06bc11975
 * and
 * http://jar.fyicenter.com/3245_JDK_11_jdk_jlink_jmod-JLink_Tool.html
 * 
 * because the jlink stuff is not exported in the module, we have to use reflection to access all that stuff
 * that reflection can access the stuff, the runtime needs to be started with "--add-opens jdk.jlink/jdk.tools.jlink.internal=jimagePatcher"
 */
package jimagePatcher;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Run {

	public static void main(String[] args) {
		
		try {
			
			if( args.length < 2) {
				System.out.println("Usage: java --add-opens jdk.jlink/jdk.tools.jlink.internal=jimagePatcher --module-path jimagePatcher.jar -m jimagePatcher/jimagePatcher.Run input-folder output-file");
				System.exit(-1);
			}
			
			String newImagePathLocation = args[1];
			Path newImagePath = Paths.get(newImagePathLocation);
			String originalImagePathLocation = args[0];
			Path originalImagePath = Paths.get(originalImagePathLocation);
			
			final Set archives = new HashSet();
			
			if (!Files.isDirectory(originalImagePath)) {
				throw new IOException("Not a directory");
			}
			System.out.println("Adding following modules:");
			System.out.println("---");
			Class classDirArchive =  Class.forName("jdk.tools.jlink.internal.DirArchive");
			Constructor constructorDirArchive = classDirArchive.getConstructor(Path.class, String.class);
			Files.walk(originalImagePath, 1).forEach((p) -> {
				if (!originalImagePath.equals(p)) {
					if (Files.isDirectory(p)) {
						try {
							System.out.println(p.getFileName());
							Object archive = constructorDirArchive.newInstance(p,p.getFileName().toString());
							archives.add(archive);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
			
			Set archives2 = Collections.unmodifiableSet(archives);
		
			// PluginsConfiguration pluginConfig = new Jlink.PluginsConfiguration(plugins, null, null)
			// PluginsConfiguration pluginConfig = new Jlink.PluginsConfiguration()
			Class classPluginsConfiguration =  Class.forName("jdk.tools.jlink.internal.Jlink$PluginsConfiguration");
			Constructor constructorPluginsConfiguration = classPluginsConfiguration.getConstructor();
			Object pluginConfig = constructorPluginsConfiguration.newInstance();
			
			// ImagePluginStack imagePluginStack = ImagePluginConfiguration.parseConfiguration(pluginConfig);
			Class classImagePluginConfiguration =  Class.forName("jdk.tools.jlink.internal.ImagePluginConfiguration");
			Method methodParseConfiguration = classImagePluginConfiguration.getDeclaredMethod("parseConfiguration", classPluginsConfiguration);
			Object imagePluginStack = methodParseConfiguration.invoke(null, pluginConfig);
		
			//
			Class classImageFileCreator =  Class.forName("jdk.tools.jlink.internal.ImageFileCreator");
			//Method methodRecreateJimage = classImagePluginConfiguration.getDeclaredMethod("recreateJimage", new Class[] {Path.class, Set.class, imagePluginStack.class});
			Method methodRecreateJimage = null;
			Method[] methods = classImageFileCreator.getDeclaredMethods();
			for (Method m : methods) {
				if(m.getName().startsWith("recreateJimage")) {
					methodRecreateJimage = m;
					break;
				}
			}
			System.out.println("---");
			System.out.println("Creating file "+newImagePathLocation);
			Object o = methodRecreateJimage.invoke(null, new Object[] {newImagePath, archives2, imagePluginStack});
			System.out.println("Done");

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	

}