#!/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`

case $1  in
	"prd")   ;;

    "sit")  ;;
    "st")  ;;
    "longrun")  ;;
  "longrun-st")  ;;


	"loc")  ;;
	"man")  ;;


	*) echo "update.sh  <prd|sit|loc|st|longrun|longrun-st|man>   "  ;exit;;

	 esac;


###svn up
git pull

ant
case $1  in
	"prd") cp -rfv $DEPLOY_DIR/build/conf/* $DEPLOY_DIR/conf/;
           cp -rfv $DEPLOY_DIR/build/conf-prd/* $DEPLOY_DIR/conf/ ;;

    "sit")  cp -rfv $DEPLOY_DIR/build/conf/* $DEPLOY_DIR/conf/;
            cp -rfv $DEPLOY_DIR/build/conf-sit/* $DEPLOY_DIR/conf/;;

    "longrun")  cp -rfv $DEPLOY_DIR/build/conf/* $DEPLOY_DIR/conf/;
            cp -fv $DEPLOY_DIR/build/conf-longrun/bin/* $DEPLOY_DIR/bin/;
	    cp -fv $DEPLOY_DIR/build/conf-longrun/conf/* $DEPLOY_DIR/conf/;
	    cp -fv $DEPLOY_DIR/build/conf-longrun/storeroot/default/* $DEPLOY_DIR/storeroot/default/;;

    "longrun-st")  cp -rfv $DEPLOY_DIR/build/conf/* $DEPLOY_DIR/conf/;
            cp -fv $DEPLOY_DIR/build/conf-longrun-st/bin/* $DEPLOY_DIR/bin/;
	    cp -fv $DEPLOY_DIR/build/conf-longrun-st/conf/* $DEPLOY_DIR/conf/;
	    cp -fv $DEPLOY_DIR/build/conf-longrun-st/storeroot/default/* $DEPLOY_DIR/storeroot/default/;;








     "st")  cp -rfv $DEPLOY_DIR/build/conf/* $DEPLOY_DIR/conf/;
            cp -rfv $DEPLOY_DIR/build/conf-st/* $DEPLOY_DIR/conf/;;
     "loc")  cp -rfv $DEPLOY_DIR/build/conf/* $DEPLOY_DIR/conf/;;
     "man")  ;;


	*) echo "update.sh  <prd|sit|loc>   "  ;exit;;

	 esac;





