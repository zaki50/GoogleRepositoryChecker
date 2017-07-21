package org.zakky.googlerepositorychecker.retrofit2.converter

import android.util.Xml
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParser
import retrofit2.Converter
import java.io.StringReader

class GroupXmlConverter : Converter<ResponseBody, List<String>> {
    companion object {
        val INSTANCE = GroupXmlConverter()
    }

    override fun convert(value: ResponseBody): List<String> {
        return parseGroupXml(value.string())
    }

    private fun parseGroupXml(xml: String): List<String> {
        val xmlParser = Xml.newPullParser()
        xmlParser.setInput(StringReader(xml))

        val result = ArrayList<String>()

        var inMetaElement = false
        var eventType = xmlParser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType != XmlPullParser.START_TAG) {
                eventType = xmlParser.next()
                continue
            }

            val name = xmlParser.name
            if (!inMetaElement) {
                if (name == "metadata") {
                    inMetaElement = true
                }
                eventType = xmlParser.next()
                continue
            }

            result.add(name)

            eventType = xmlParser.next()
        }
        return result
    }

}