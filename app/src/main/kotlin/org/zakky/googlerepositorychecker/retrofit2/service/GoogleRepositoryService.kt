package org.zakky.googlerepositorychecker.retrofit2.service

import kotlinx.coroutines.Deferred
import org.zakky.googlerepositorychecker.model.Artifact
import org.zakky.googlerepositorychecker.retrofit2.converter.ArtifactXml
import org.zakky.googlerepositorychecker.retrofit2.converter.GroupXml
import retrofit2.http.GET
import retrofit2.http.Path


interface GoogleRepositoryService {

    companion object {
        fun toPath(groupName: String): String = groupName.replace('.', '/')
    }

    @GET("master-index.xml")
    @GroupXml
    suspend fun listGroups(): List<String>

    @GET("{groupPath}/group-index.xml")
    @ArtifactXml
    suspend fun listArtifact(@Path("groupPath", encoded = true) groupPath: String): List<Artifact>
}