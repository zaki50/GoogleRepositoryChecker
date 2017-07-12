package org.zakky.googlerepositorychecker.retrofit2.service

import io.reactivex.Single
import org.zakky.googlerepositorychecker.retrofit2.converter.ArtifactXml
import org.zakky.googlerepositorychecker.retrofit2.converter.GroupXml
import org.zakky.googlerepositorychecker.retrofit2.model.Artifact
import retrofit2.http.GET
import retrofit2.http.Path


interface GoogleRepositoryService {

    companion object {
        fun toPath(groupName: String): String = groupName.replace('.', '/')
    }

    @GET("master-index.xml")
    @GroupXml
    fun listGroups(): Single<List<String>>

    @GET("{groupPath}/group-index.xml")
    @ArtifactXml

    fun listArtifact(@Path("groupPath", encoded = true) groupPath: String): Single<List<Artifact>>
}