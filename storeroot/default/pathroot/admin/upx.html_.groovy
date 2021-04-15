import com.mapkc.nsfw.model.ActionHandler
import com.mapkc.nsfw.model.RenderContext

public class Upx implements ActionHandler {

    @Override
    boolean filterAction(RenderContext rc) {

        if (rc.isPost()) {

            io.netty.handler.codec.http.multipart.FileUpload fileUpload = rc.getFileUpload('file')

            //  File file = fileUpload.file;
            ObjectInputStream objectInputStream = null
            if (!fileUpload.isInMemory()) {

                objectInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fileUpload.file)))
            } else {
                objectInputStream = new ObjectInputStream(new ByteArrayInputStream(fileUpload.get()))
            }

            while (true) {


                String id = objectInputStream.readUTF();
                if (id.equals('')) {
                    break
                }
                Map<String, String> attr = objectInputStream.readObject()
                if (!rc.site.siteStore.exists(id)) {
                    rc.site.siteStore.create(id)
                }

                rc.site.siteStore.saveAttributes(id, attr)
            }
            if (objectInputStream) {
                objectInputStream.close()
            }
            if (!fileUpload.isInMemory()) {
                fileUpload.file.delete()
            }


            rc.setVar('msg','Upload OK!')



//            ObjectInputStream objectInputStream=null
//
//            if (
//            fileUpload.isInMemory()) {
//                objectInputStream =new ObjectInputStream(new ByteArrayInputStream(fileUpload.get()))
//            }else {
//
//            }


             

            //eturn true

        }
        return false
    }
}