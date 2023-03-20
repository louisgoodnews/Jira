
import groovy.json.JsonSlurper;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.TrustedRequestFactory;
import com.onresolve.scriptrunner.runner.customisers.PluginModule;

private Map connect(String url, Map<String, String> requestHeaderMap){
        
    @PluginModule
    TrustedRequestFactory trustedRequestFactory

    Request request = trustedRequestFactory.createTrustedRequest(Request.MethodType.CONNECT, url);
    if(requestHeaderMap){

        for(String requestHeader : requestHeaderMap.keySet()){

            request.addHeader(requestHeader, requestHeaderMap.get(requestHeader));
        }
    }
    return new JsonSlurper().parseText(request.execute()) as Map;
}