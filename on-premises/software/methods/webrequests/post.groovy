
import groovy.json.JsonSlurper;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.TrustedRequestFactory;
import com.onresolve.scriptrunner.runner.customisers.PluginModule;

private Map post(String url, Map<String, String> requestHeaderMap, Map<String, String> requestBodyMap){
        
    @PluginModule
    TrustedRequestFactory trustedRequestFactory

    Request request = trustedRequestFactory.createTrustedRequest(Request.MethodType.POST, url);
    if(requestHeaderMap){

        for(String requestHeader : requestHeaderMap.keySet()){

            request.addHeader(requestHeader, requestHeaderMap.get(requestHeader));
        }
    }
    request.setRequestBody(requestBodyMap);
    return new JsonSlurper().parseText(request.execute()) as Map;
}