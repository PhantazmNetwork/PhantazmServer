plugins {
    id("phantazm.java-library-conventions")
}

repositories {
    maven("https://jitpack.io")
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
pluginManager.withPlugin("java") {
    val libs = catalogs.named("libs")
    dependencies.addProvider("api", libs.findLibrary("minestom").get())
}
