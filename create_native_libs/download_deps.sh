if [ -d "./tmp" ]; then
  rm -rf "./tmp"
fi

mkdir tmp
cd tmp

wget "http://jogamp.org/deployment/jogamp-current/archive/jogamp-linux-amd64.7z"
wget "http://jogamp.org/deployment/jogamp-current/archive/jogamp-linux-i586.7z"
wget "http://jogamp.org/deployment/jogamp-current/archive/jogamp-macosx-universal.7z"
wget "http://jogamp.org/deployment/jogamp-current/archive/jogamp-windows-amd64.7z"
wget "http://jogamp.org/deployment/jogamp-current/archive/jogamp-windows-i586.7z"
wget "http://jogamp.org/deployment/jogamp-current/archive/jogamp-solaris-amd64.7z"
wget "http://jogamp.org/deployment/jogamp-current/archive/jogamp-solaris-i586.7z"

for file in *.7z
do
  p7zip -d $file
done

mkdir lib

for dir in `ls . | grep jogamp`
do
  if [ -d "./$dir" ]; then
    mv ./$dir/jar/*.jar ./lib/
  fi
done

echo "Creating JAR file"
jar -cMf jogl-native-deps-2.0-rc5.jar lib

echo "Uploading to clojars"
scp ../pom.xml jogl-native-deps-2.0-rc5.jar clojars@clojars.org:

cd ..
rm -rf "./tmp"
