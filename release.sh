echo "temporary release script, need to get continous deployment running"
rm -rf tmp
git clone -o github -b gh-pages https://github.com/baloise/egitblit.git tmp
cd tmp/updatesite
rm -rf *
cp -R ../../com.baloise.egitblit.site/target/site/* .
git add --all .
git commit -m release
echo "now you need to git push"
sleep 60