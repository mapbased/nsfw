import com.mapkc.nsfw.handler.BaseRPCActionHandler
import com.mapkc.nsfw.model.RenderContext
import io.netty.util.internal.PlatformDependent

public class APrint extends BaseRPCActionHandler {

    Thread thread

    @Override
    boolean filterAction(RenderContext rc) {

        if (!thread) {
            thread = new Thread(new Runnable() {
                @Override
                void run() {
                    while (true) {
                         println(PlatformDependent.DIRECT_MEMORY_COUNTER.get())
                        sleep(1000)
                    }

                }
            })
            println("Started..")
            thread.start()
        }


        return super.filterAction(rc)
    }
}