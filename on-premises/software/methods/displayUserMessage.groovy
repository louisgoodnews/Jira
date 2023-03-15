import com.onresolve.scriptrunner.runner.util.UserMessageUtil;

private void displayUserMessage(String messageType, String messageBody, String closeType){
    /*
        *   "messageType" has to be either "info", "success", "warning" or "error" 
        *   "closeType" has to be either "manual" or "auto"
    */
    UserMessageUtil.flag(["type": messageType,"body": messageBody, "close": closeType]);
}