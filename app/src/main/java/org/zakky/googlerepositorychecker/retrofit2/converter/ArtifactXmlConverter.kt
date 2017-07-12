package org.zakky.googlerepositorychecker.retrofit2.converter

import android.util.Xml
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParser
import org.zakky.googlerepositorychecker.retrofit2.model.Artifact
import retrofit2.Converter
import java.io.StringReader

class ArtifactXmlConverter : Converter<ResponseBody, List<Artifact>> {
    companion object {
        val INSTANCE = ArtifactXmlConverter()
    }

    override fun convert(value: ResponseBody): List<Artifact> {
        return parseArtifactXml(value.string())
    }

    private fun parseArtifactXml(xml: String): List<Artifact> {
        val xmlParser = Xml.newPullParser()
        xmlParser.setInput(StringReader(xml))

        val result = ArrayList<Artifact>()

        var groupName: String? = null
        var eventType = xmlParser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType != XmlPullParser.START_TAG) {
                eventType = xmlParser.next()
                continue
            }

            if (groupName == null) {
                groupName = xmlParser.name
                eventType = xmlParser.next()
                continue
            }

            val artifactName = xmlParser.name
            for (i in 0..xmlParser.attributeCount) {
                if (xmlParser.getAttributeName(i) != "versions") {
                    continue
                }
                val versions = xmlParser.getAttributeValue(i)
                result.add(Artifact(groupName, artifactName, versions.split(",")))
                break
            }
            eventType = xmlParser.next()
        }
        return result
    }
}