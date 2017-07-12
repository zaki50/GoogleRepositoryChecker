package org.zakky.googlerepositorychecker.retrofit2.model

data class Artifact(val groupName: String,
               val artifactName: String,
               val versions: List<String>)