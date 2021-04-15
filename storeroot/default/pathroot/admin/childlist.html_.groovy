import com.mapkc.nsfw.handler.BaseRPCActionHandler
import com.mapkc.nsfw.model.RenderContext
import com.mapkc.nsfw.model.Site
import com.mapkc.nsfw.model.XEnum
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.handler.codec.http.HttpHeaders

public class ChildListHtml extends BaseRPCActionHandler {


    @Override
    boolean filterAction(RenderContext rc) {

        if (rc.param('exportzip').equals('true')) {
            String path = rc.param('id', '/');

            XEnum x = rc.e(path)
            if (!x) {
                rc.sendResponseHtml("Cannot find id:" + path)
                return true
            }


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            //ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)
            write(path, objectOutputStream, rc.site)
            objectOutputStream.writeUTF('')

            objectOutputStream.close()


            rc.addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/zip");
            String name = x.name
            if (!name) {
                name = 'root'
            }
            rc.addHeader("Content-Disposition", "attachment; filename=${name}.nsf.exp");

            ByteBuf buf = Unpooled.copiedBuffer(byteArrayOutputStream.toByteArray())



            rc.resp.setContent(buf);
            // rc.addHeadersForShortResp()
            rc.resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes());
            //rc.getChannel().writeAndFlush(rc.resp)
           // rc.finish()
            rc.getChannel().writeAndFlush(rc.resp).addListener(ChannelFutureListener.CLOSE)



            return true;


        }


        return super.filterAction(rc)
    }

    private void write(String id, ObjectOutputStream outputStream, Site site) {
        if (id.equals('/static')) {
            return
        }

        Map<String, String> attr = site.getSiteStore().getAttributes(id)
        if (attr == null) {
            return
        }

        outputStream.writeUTF(id)


        outputStream.writeObject(attr)

        // zipOutputStream.write(byteArrayOutputStream.toByteArray())

        for (String s : site.getSiteStore().getChildren(id)) {
            write("${id.equals('/') ? '' : id}/${s}", outputStream, site)
        }


    }

}