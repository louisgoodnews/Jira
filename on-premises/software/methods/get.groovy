
import groovy.json.JsonSlurper;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.TrustedRequestFactory;
import com.onresolve.scriptrunner.runner.customisers.PluginModule;

@PluginModule
TrustedRequestFactory trustedRequestFactory

private Map get(String url, Map<String, String> requestHeaderMap){

    Request request = trustedRequestFactory.createtrustedRequest(Request.MethodType.GET, url);
    for(String requestHeader : requestHeaderMap.keySet()){

        request.addHeader(requestHeader, requestHeaderMap.get(requestHeader))
    }
    return new JsonSlurper().parseText(request.execute()) as Map;
}