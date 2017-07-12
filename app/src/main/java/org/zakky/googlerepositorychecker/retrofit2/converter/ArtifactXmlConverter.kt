package org.zakky.googlerepositorychecker.retrofit2.converter

import okhttp3.ResponseBody
import retrofit2.Converter

class ArtifactXmlConverter : Converter<ResponseBody, List<String>> {
    companion object {
        val INSTANCE = ArtifactXmlConverter()
    }

    override fun convert(value: ResponseBody?): List<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}