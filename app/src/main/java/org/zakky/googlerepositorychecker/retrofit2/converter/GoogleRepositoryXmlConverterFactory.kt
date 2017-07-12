package org.zakky.googlerepositorychecker.retrofit2.converter

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class GoogleRepositoryXmlConverterFactory : Converter.Factory() {
    companion object {
        fun create() : GoogleRepositoryXmlConverterFactory {
            return GoogleRepositoryXmlConverterFactory()
        }
    }

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>,
                                       retrofit: Retrofit): Converter<ResponseBody, *>? {
        annotations.forEach { annotation ->
            when (annotation) {
                is ArtifactXml -> return ArtifactXmlConverter.INSTANCE
                is GroupXml -> return GroupXmlConverter.INSTANCE
            }
        }
        return null
    }

}