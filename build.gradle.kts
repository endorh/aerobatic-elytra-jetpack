import net.minecraftforge.gradle.userdev.tasks.RenameJarInPlace
import java.text.SimpleDateFormat
import java.util.*

buildscript {
	repositories {
		maven("https://files.minecraftforge.net/maven")
		mavenCentral()
	}
	dependencies {
		classpath("net.minecraftforge.gradle:ForgeGradle:5.1.+") {
			isChanging = true
		}
	}
}

// Plugins
plugins {
	java
	id("net.minecraftforge.gradle")
	`maven-publish`
	// id("com.github.johnrengelman.shadow") version "7.1.2"
}

// Mod info --------------------------------------------------------------------

val modId = "aerobaticelytrajetpack"
val modGroup = "endorh.aerobaticelytra.jetpack"
val githubRepo = "endorh/aerobaticelytrajetpack"
val modVersion = "0.2.20"
val mcVersion = "1.18.2"
val forge = "40.1.0"
val forgeVersion = "$mcVersion-$forge"
val mappingsChannel = "official"
val mappingsVersion = "1.18.2"

group = modGroup
version = modVersion
val groupSlashed = modGroup.replace(".", "/")
val classname = "AerobaticElytraJetpack"
val modArtifactId = "$modId-$mcVersion"
val modMavenArtifact = "$modGroup:$modArtifactId:$modVersion"

// Attributes
val displayName = "Aerobatic Elytra - Jetpack"
val vendor = "Endor H"
val credits = ""
val authors = "Endor H"
val issueTracker = ""
val page = ""
val updateJson = ""
val logoFile = "$modId.png"
val modDescription = """
	Adds a Jetpack flight mode to the Aerobatic Elytra.
""".trimIndent()

// License
val license = "LGPL"

// Dependencies
val aerobaticElytraVersion = "0.2.+"
val flightCoreVersion = "0.5.+"
val simpleConfigApiVersion = "1.0.+"
val simpleConfigVersion = "1.0.+"
val lazuLibVersion = "0.5.+"

// Integration
val jeiVersion = "9.7.1.255"
val curiosVersion = "1.18.2-5.0.7.1"
val caelusVersion = "1.18.1-3.0.0.2"

val jarAttributes = mapOf(
	"Specification-Title"      to modId,
	"Specification-Vendor"     to vendor,
	"Specification-Version"    to "1",
	"Implementation-Title"     to project.name,
	"Implementation-Version"   to version,
	"Implementation-Vendor"    to vendor,
	"Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
	"Maven-Artifact"           to modMavenArtifact
)

val modProperties = mapOf(
	"modid"         to modId,
	"display"       to displayName,
	"version"       to version,
	"mcversion"     to mcVersion,
	"vendor"        to vendor,
	"authors"       to authors,
	"credits"       to credits,
	"license"       to license,
	"page"          to page,
	"issue_tracker" to issueTracker,
	"update_json"   to updateJson,
	"logo_file"     to logoFile,
	"description"   to modDescription,
	"modGroup"      to modGroup,
	"class_name"    to classname,
	"group_slashed" to groupSlashed
)

// Source Sets -----------------------------------------------------------------

sourceSets.main.get().resources {
	// Include resources generated by data generators.
	srcDir("src/generated/resources")
}

// Java options ----------------------------------------------------------------

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

println(
	"Java: " + System.getProperty("java.version")
	+ " JVM: " + System.getProperty("java.vm.version") + "(" + System.getProperty("java.vendor")
	+ ") Arch: " + System.getProperty("os.arch"))

// Minecraft options -----------------------------------------------------------

minecraft {
	mappings(mappingsChannel, mappingsVersion)
	
	// Run configurations
	runs {
		val client = create("client") {
			workingDirectory(file("run"))
			
			// Allowed flags: SCAN, REGISTRIES, REGISTRYDUMP
			property("forge.logging.markers", "REGISTRIES")
			property("forge.logging.console.level", "debug")
			property("mixin.env.disableRefMap", "true")
			
			jvmArg("-XX:+AllowEnhancedClassRedefinition")
			
			mods {
				create(modId) {
					source(sourceSets.main.get())
				}
			}
		}
		
		create("server") {
			workingDirectory(file("run"))
			
			// Allowed flags: SCAN, REGISTRIES, REGISTRYDUMP
			property("forge.logging.markers", "REGISTRIES")
			property("forge.logging.console.level", "debug")
			property("mixin.env.disableRefMap", "true")
			
			jvmArg("-XX:+AllowEnhancedClassRedefinition")
			
			arg("nogui")
			
			mods {
				create(modId) {
					source(sourceSets.main.get())
				}
			}
		}
		
		create("client2") {
			parent(client)
			args("--username", "Dev2")
		}
	}
}

// Project dependencies -----------------------------------------------------------

repositories {
	maven("https://repo.maven.apache.org/maven2") {
		name = "Maven Central"
	}
	maven("https://www.cursemaven.com") {
		name = "Curse Maven" // Curse Maven
		content {
			includeGroup("curse.maven")
		}
	}
	
	maven("https://dvs1.progwml6.com/files/maven/") {
		name = "Progwml6 maven" // JEI
	}
	maven("https://modmaven.k-4u.nl") {
		name = "ModMaven" // JEI fallback
	}
	
	maven("https://maven.theillusivec4.top/") {
		name = "TheIllusiveC4" // Curios API
	}
	
	maven(rootProject.projectDir.parentFile.resolve("maven")) {
		name = "LocalMods" // Local repository
	}
	mavenCentral()
}

dependencies {
	// IDE
	implementation("org.jetbrains:annotations:23.0.0")
    implementation("org.junit.jupiter:junit-jupiter:5.9.0")

	// Minecraft
    "minecraft"("net.minecraftforge:forge:$forgeVersion")
	
	// Mod dependencies
	// Aerobatic Elytra
	// TODO: Replace with curse maven or GitHub maven once published
	implementation(fg.deobf("endorh.aerobaticelytra:aerobaticelytra-$mcVersion:$aerobaticElytraVersion"))
	
	// Flight Core
	implementation(fg.deobf("endorh.flightcore:flightcore-$mcVersion:$flightCoreVersion"))
	
	// Simple Config
	compileOnly("endorh.simpleconfig:simpleconfig-$mcVersion-api:$simpleConfigApiVersion")
	runtimeOnly(fg.deobf("endorh.simpleconfig:simpleconfig-$mcVersion:$simpleConfigVersion"))
	
	// Endor8 Util
	implementation(fg.deobf("endorh.util.lazulib:lazulib-$mcVersion:$lazuLibVersion"))

	// Only for debug
	runtimeOnly(fg.deobf("mezz.jei:jei-$mcVersion:$jeiVersion"))

	// Curios API
	// runtimeOnly(fg.deobf("top.theillusivec4.curios:curios-forge:$curiosVersion"))

	// Caelus API
	// runtimeOnly(fg.deobf("top.theillusivec4.caelus:caelus-forge:$caelusVersion"))

	// Curious Elytra
	// runtimeOnly(fg.deobf("curse.maven:elytra-slot-317716:3601975"))

	// Colytra
	// runtimeOnly(fg.deobf("curse.maven:colytra-280200:3725170"))

	// Customizable Elytra
	// runtimeOnly(fg.deobf("curse.maven:customizableelytra-440047:3728574"))

	// Additional Banners
	// runtimeOnly("curse.maven:bookshelf-228525:3900932:deobf")
	// runtimeOnly("curse.maven:additionalbanners-230137:3835686:deobf")
	
	// Catalogue
	// runtimeOnly(fg.deobf("curse.maven:catalogue-459701:3803098"))
}

// Tasks --------------------------------------------------------------------------

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.classes {
	dependsOn(tasks.extractNatives.get())
}

lateinit var reobfJar: RenameJarInPlace
reobf {
	reobfJar = create("jar")
}

// Jar attributes
tasks.jar {
	archiveBaseName.set(modArtifactId)
	
	manifest {
		attributes(jarAttributes)
	}
	
	finalizedBy(reobfJar)
}

val sourcesJarTask = tasks.register<Jar>("sourcesJar") {
	group = "build"
	archiveBaseName.set(modArtifactId)
	archiveClassifier.set("sources")
	
	from(sourceSets.main.get().allJava)
	
	manifest {
		attributes(jarAttributes)
		attributes(mapOf("Maven-Artifact" to "$modMavenArtifact:${archiveClassifier.get()}"))
	}
}

val deobfJarTask = tasks.register<Jar>("deobfJar") {
	group = "build"
	archiveBaseName.set(modArtifactId)
	archiveClassifier.set("deobf")
	
	from(sourceSets.main.get().output)
	
	manifest {
		attributes(jarAttributes)
		attributes(mapOf("Maven-Artifact" to "$modMavenArtifact:${archiveClassifier.get()}"))
	}
}

// Process resources
tasks.processResources {
	inputs.properties(modProperties)
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
	
	// Exclude development files
	exclude("**/.dev/**")
	
	from(sourceSets.main.get().resources.srcDirs) {
		// Expand properties in manifest files
		filesMatching(listOf("**/*.toml", "**/*.mcmeta")) {
			expand(modProperties)
		}
		// Expand properties in JSON resources except for translations
		filesMatching("**/*.json") {
			if (!path.contains("/lang/"))
				expand(modProperties)
		}
	}
}

// Make the clean task remove the run and logs folder
tasks.clean {
	delete("run")
	delete("logs")
	dependsOn(saveModsTask)
	dependsOn(cleanBuildAssetsTask)
	finalizedBy(setupMinecraftTask)
}

val saveModsTask = tasks.register<Copy>("saveMods") {
	from("run/mods")
	into("saves/mods")
}

val setupMinecraftTask = tasks.register<Copy>("setupMinecraft") {
	from("saves")
	into("run")
}

val cleanBuildAssetsTask = tasks.register<Delete>("cleanBuildAssets") {
	delete("build/resources/main/assets")
}

// Make the clean task remove the run and logs folder
tasks.clean {
	delete("run")
	delete("logs")
	dependsOn(saveModsTask)
	dependsOn(cleanBuildAssetsTask)
	finalizedBy(setupMinecraftTask)
}

// Publishing ------------------------------------------------------------------

artifacts {
	archives(tasks.jar.get())
	archives(sourcesJarTask)
	archives(deobfJarTask)
}

publishing {
	repositories {
		maven("https://maven.pkg.github.com/$githubRepo") {
			name = "GitHubPackages"
			credentials {
				username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
				password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
			}
		}
		
		maven(rootProject.projectDir.parentFile.resolve("maven")) {
			name = "LocalMods"
		}
	}
	
	publications {
		register<MavenPublication>("mod") {
			artifactId = "$modId-$mcVersion"
			version = modVersion
			
			artifact(tasks.jar.get())
			artifact(sourcesJarTask)
			artifact(deobfJarTask)
			
			pom {
				name.set(displayName)
				url.set(page)
				description.set(modDescription)
			}
		}
	}
}