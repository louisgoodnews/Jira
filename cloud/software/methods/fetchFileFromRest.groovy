import java.net.URL
import java.net.HttpURLConnection

private static File fetchFileFromRest(String urlStr, String savePath) {
    URL url = new URL(urlStr)
    HttpURLConnection connection = (HttpURLConnection) url.openConnection()
    connection.setRequestMethod("GET")
    InputStream inputStream = connection.getInputStream()

    File file = new File(savePath)
    FileOutputStream outputStream = new FileOutputStream(file)
    byte[] buffer = new byte[4096]
    int bytesRead = -1
    while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead)
    }
    outputStream.close()
    inputStream.close()

    return file
}
