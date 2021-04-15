package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.input.FormFieldModel;
import com.mapkc.nsfw.model.RenderContext;

import java.util.List;

/**
 * Created by chy on 14-8-27.
 */
public class CategorySelect extends BaseRPCActionHandler {

    @Override
    public boolean filterAction(RenderContext rc) {
        return false;
    }

    public static final class CateItem {

        public String name;
        public String code;
        public int cid;
        public boolean selected;
    }

    public static interface CategoryDataProvider {

        public List<CateItem> getValueListData(RenderContext rc, int parentId);

        List<CateItem>[] getDatas(RenderContext rc, FormFieldModel ffm);

    }
//
//	@AutoAssign(path = "/ds/hyq/category")
//	VolatileBag<Schema> cate;

    @JsRPCMethod(access = AccessMode.Public)
    public List<CateItem> getValueListData(RenderContext rc, int parentId, String ffmpath) {

        FormFieldModel ffm = (FormFieldModel) rc.getSite().getXEnum(ffmpath);
        CategoryDataProvider cp = (CategoryDataProvider) ffm.attrObj("dataclass", rc.getSite());
        return cp.getValueListData(rc, parentId);


        //return cate.getValue().listObjectBySql(CateItem.class, "parent=? order by sort asc", new Object[]{parentId});

    }

    public List<CateItem>[] getDatas(RenderContext rc) {
        FormFieldModel ffm = (FormFieldModel) rc.v("fk-fm");
        CategoryDataProvider cp = (CategoryDataProvider) ffm.attrObj("dataclass", rc.getSite());
        return cp.getDatas(rc, ffm);

//		FormFieldModel ffm = (FormFieldModel) rc.v("fk-fm");
//		int root = ffm.attrInt("root");
//		int cid = root;
//		try {
//			cid = Integer.parseInt(ffm.singleParamValue(rc));
//		} catch (NumberFormatException e) {
//
//		}
//		int levelCnt = ffm.attrInt("levelcnt", 3);
//		int colcnt = 12 / levelCnt;
//		rc.setVar("colcnt", colcnt);
//
//		String code = String.valueOf(cate.getValue().getField("code", String.valueOf(cid)));
//
//
//		List<CateItem>[] rt = new ArrayList[levelCnt];
//		rt[0] = this.getValueListData(rc, root);
//		for (int i = 1; i < levelCnt; i++) {
//
//			List<CateItem> up = rt[i - 1];
//			int tmpcid = -1;
//			for (CateItem ci : up) {
//				if (code.startsWith(ci.code)) {
//					tmpcid = ci.cid;
//					ci.selected = true;
//					break;
//				}
//			}
//			if (tmpcid > 0) {
//				rt[i] = this.getValueListData(rc, tmpcid);
//			} else {
//				rt[i] = new ArrayList(0);
//			}
//
//
//		}
//		for (CateItem ci : rt[rt.length - 1]) {
//			if (code.startsWith(ci.code)) {
//
//				ci.selected = true;
//				break;
//			}
//		}
//
//
//		return rt;
    }

}
