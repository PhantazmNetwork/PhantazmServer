plugins {
    base
}

tasks.getByName<Delete>("clean") {
    delete("$rootDir/run/server-1/libs")
}