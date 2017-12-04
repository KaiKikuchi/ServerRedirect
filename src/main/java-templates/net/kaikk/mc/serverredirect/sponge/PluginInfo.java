package ${package}.sponge;

public class PluginInfo {
	public static final String id = "${lowercaseid}";
	public static final String name = "${project.artifactId}";
	public static final String version = "${project.version}";
	public static final String description = "${description}";
	public static final String author = "${developer}";
	
	private PluginInfo(){}
}
