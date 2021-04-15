import com.google.common.io.ByteStreams
import com.mapkc.nsfw.handler.AccessMode
import com.mapkc.nsfw.handler.BaseRPCActionHandler
import com.mapkc.nsfw.handler.JsRPCMethod
import com.mapkc.nsfw.model.RenderContext

public class Exec extends BaseRPCActionHandler {


    @JsRPCMethod(access = AccessMode.Admin)

    public String exe(RenderContext renderContext, String cmd) {


        Process process = Runtime.runtime.exec(["/bin/sh", "-c", cmd].toArray(new String[0]))
        InputStream inputStream = process.getInputStream();
        InputStream errorStream = process.errorStream;

        Byte[] bytes = ByteStreams.toByteArray(ByteStreams.limit(inputStream, 100 * 1024))
        Byte[] errors = ByteStreams.toByteArray(ByteStreams.limit(errorStream, 100 * 1024))

       

        errorStream.close()
        inputStream.close()
        process.destroy()

        return new String(bytes) + new String(errors)
    }
}