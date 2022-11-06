plugins {
    id("phantazm.java-library-conventions")
}

repositories {
    exclusiveContent {
        forRepository {
            maven("https://jitpack.io")
        }
        filter {
            includeModule("com.github.PhantazmNetwork.Minestom", "Minestom")
            includeGroup("com.github.Minestom")
            includeGroup("com.github.MadMartian")
        }
    }
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
pluginManager.withPlugin("java") {
    val libs = catalogs.named("libs")
    dependencies.addProvider("api", libs.findLibrary("minestom").get())
}
