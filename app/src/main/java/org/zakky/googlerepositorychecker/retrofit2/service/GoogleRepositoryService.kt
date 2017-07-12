package org.zakky.googlerepositorychecker.retrofit2.service

import io.reactivex.Single
import org.zakky.googlerepositorychecker.retrofit2.converter.ArtifactXml
import org.zakky.googlerepositorychecker.retrofit2.converter.GroupXml
import retrofit2.http.GET


interface GoogleRepositoryService {

    @GET("master-index.xml")
    @GroupXml
    fun listGroups(): Single<List<String>>

    @GET("\${groupPath}/group-index.xml")
    @ArtifactXml
    fun listArtifact(groupPath: String): Single<List<String>/* FIXME */>
}