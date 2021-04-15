//package com.dianziq.nsfw.model;
//
//import java.io.IOException;
//import java.util.Map;
//
//import DefaultFormInput;
//import FormHandler;
//import ESLogger;
//import Loggers;
//import DefaultValidator;
//
//public enum XEnumType {
//
//	Fragment() {
//
//
//		@Override
//		protected XEnum createObject() {
//			return new Fragment();
//		}
//
//
//	},
//
//	FormInput() {
//
//		@Override
//		protected XEnum createObject() {
//			return new DefaultFormInput();
//		}
//
//	},
//
//	XEnum() {
//
//
//		@Override
//		protected XEnum createObject() {
//			// TODO Auto-generated method stub
//			return new XEnum();
//		}
//
//	},
//	Page() {
//
//
//		@Override
//		protected XEnum createObject() {
//
//			return new Page();
//		}
//
//
//
//	},
//
//	FormModel() {
//
//
//		@Override
//		protected XEnum createObject() {
//
//			return new FormModel();
//		}
//
//
//	},
//	FormFieldModel() {
//
//		@Override
//		protected XEnum createObject() {
//
//			return new FormFieldModel();
//		}
//
//
//	},
//	Schema() {
//
//
//		@Override
//		protected XEnum createObject() {
//
//			return new Schema();
//		}
//
//
//	},
//	SchemaField() {
//
//
//		@Override
//		protected XEnum createObject() {
//
//			return new SchemaField();
//		}
//
//
//	},
//	DataSource() {
//		@Override
//		protected XEnum createObject() {
//
//			return new DataSource();
//		}
//
//
//
//	},
//
//	StaticRoot() {
//
//		@Override
//		protected XEnum createObject() {
//			// TODO Auto-generated method stub
//			return new StaticRoot();
//		}
//
//
//
//	},
//	Validator() {
//		@Override
//		protected XEnum createObject() {
//			// TODO Auto-generated method stub
//			return new DefaultValidator();
//		}
//	},
//	FormableBag() {
//		@Override
//		protected XEnum createObject() {
//			// TODO Auto-generated method stub
//			return new FormableBag();
//		}
//	}
//
//	;
//	private FormModel buildinfm = createBuildin();
//
//	private FormModel createBuildin() {
//		FormModel fm = FormModel
//				.fromClass(this
//						.createObject().getClass());
//		final FormHandler old = fm.handler;
//		fm.handler = new FormHandler() {
//
//			@Override
//			public Map<String, String> load(RenderContext rc, String id,
//					FormModel model) throws IOException {
//				return old.load(rc, id, model);
//
//			}
//
//			@Override
//			public boolean update(FormModel model,
//					String id, RenderContext rc, Map<String, String> values)
//							throws IOException {
//				values.put(XEnum.KnownAttributes.Type
//						.name(), XEnumType.this.name());
//				return old.update(model, id, rc, values);
//
//			}
//
//		};
//
//		return fm;
//	}
//
//	final public FormModel getFormModel() {
//
//		return buildinfm;
//	}
//
//	protected abstract XEnum createObject();
//
//	public String getIcon() {
//		return "folder";
//	}
//
//	// static XEnumType detectType(String id, Map<String, String> attributes) {
//	//
//	// String s = attributes.getTarget(KnownAttributes.Type.name());
//	// if (s == null) {
//	// if (id.startsWith("/")) {
//	// return id
//	// .endsWith(Site.FRAGMENT_EXT) ? XEnumType.Fragment : id
//	// .endsWith(Site.MASTER_EXT) ? XEnumType.Page : id
//	// .endsWith(Site.PAGE_EXT) ? XEnumType.Page
//	// : XEnumType.XEnum;
//	//
//	// } else {
//	// return XEnumType.XEnum;
//	// }
//	// }
//	// XEnumType type = XEnumType.valueOf(s);
//	// return type;
//	//
//	// }
//
//
//
//	/**
//	 * when id is null,must provide parentId and name from attributes
//	 *
//	 * @param s
//	 * @param id
//	 * @param attributes
//	 * @return
//	 */
//	// static final public XEnum createObj(Site s, String id,
//	// Map<String, String> attributes) {
//	// if (attributes == null) {
//	// return null;
//	// }
//	// // log.debug("creating XEnum:{}", id);
//	//
//	// String parent;
//	// String name;
//	// if(id==null){
//	// parent = attributes.getTarget("parentId");
//	// name = attributes.getTarget("name");
//	// id = parent + "/" + name;
//	//
//	// } else {
//	// int i = id.lastIndexOf('/');
//	// name = id.substring(Math.min(i + 1, id.length()));
//	// parent = i == 0 ? "/" : id.substring(0, i);
//	// }
//	//
//	// XEnumType t = null;
//	// XEnum o = null;
//	// FormModel fm = null;
//	//
//	// String className = attributes.getTarget("className");
//	// if (className != null && className.length() > 0) {
//	// Object lo;
//	// try {
//	// lo = DynamicClassLoader.load(className, s);
//	// } catch (ClassNotFoundException e) {
//	//
//	// lo = null;
//	// log.error("Cannot load class:{}", className);
//	//
//	// }
//	// if (lo instanceof XEnum) {
//	// o = (XEnum) lo;
//	// t = o.getXEnumType();
//	// fm = FormModel.fromClass(lo.getClass());
//	// }
//	// }
//	//
//	// if (o == null) {
//	// t = detectType(id, attributes);
//	//
//	// o = t.createObject();
//	// fm = t.getFormModel();
//	// }
//	//
//	// fm.assign(o, attributes, s);
//	//
//	// o.name = name;
//	// o.parentId = parent;
//	// o.attributes = attributes;
//	// o.init(s);
//	//
//	// return o;
//	//
//	// }
//
//	final static ESLogger log = Loggers.getLogger(XEnumType.class);
//
//
//	// public List getFormFields() {
//	// return null;
//	// }
//
// }
