package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.Renderable;

import java.util.HashMap;

public class Components {
    private static interface Creator {
        public Renderable create();
    }

    private java.util.HashMap<String, Creator> map = new HashMap<String, Creator>();

    public static final Components INSTANCE = new Components();

    private Components() {

        map.put("import", new Creator() {
            @Override
            public Renderable create() {
                return new Import();
            }

        });
        map.put("dynimport", new Creator() {
            @Override
            public Renderable create() {
                return new DynamicImport();
            }

        });
        map.put("code", new Creator() {
            @Override
            public Renderable create() {
                return new Code();
            }

        });

        map.put("inline", new Creator() {
            @Override
            public Renderable create() {
                return new Inline();
            }
        });

        map.put("form", new Creator() {
            @Override
            public Renderable create() {
                return new Form();
            }
        });
        map.put("insert", new Creator() {
            @Override
            public Renderable create() {
                return new Insert();
            }
        });
        map.put("for", new Creator() {
            @Override
            public Renderable create() {
                return new For();
            }
        });
        map.put("head", new Creator() {
            @Override
            public Renderable create() {
                return Head.INSTANCE;
            }
        });
        map.put("tail", new Creator() {
            @Override
            public Renderable create() {
                return Tail.INSTANCE;
            }
        });
        map.put("setvar", new Creator() {
            @Override
            public Renderable create() {
                return new SetVar();
            }
        });
        map.put("tohead", new Creator() {
            @Override
            public Renderable create() {
                return new ToHead();
            }
        });
        map.put("totail", new Creator() {
            @Override
            public Renderable create() {
                return new ToTail();
            }
        });

        map.put("if", new Creator() {
            @Override
            public Renderable create() {
                return new If();
            }
        });
        // map.put("tree", new Creator() {
        // @Override
        // public Renderable create() {
        // return new Tree();
        // }
        // });
        // map.put("import", new Creator() {
        // @Override
        // public Renderable create() {
        // return new Import();
        // }
        // });
        map.put("query", new Creator() {
            @Override
            public Renderable create() {
                return new Query();
            }
        });

    }

    public Renderable create(String type, LoadContext lc) {

        Creator c = this.map.get(type.toLowerCase());
        if (c == null) {
            throw new java.lang.RuntimeException("Cannot find component:"
                    + type);
        }
        return c.create();

    }
}
