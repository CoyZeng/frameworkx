curr=$(cd "$(dirname "$0")" && pwd)
root=${curr:0:${#curr}-7}
gradle \
  :mybatisx:upload \
  -p ${root}/ \
  -c ${root}/settings/mybatisx.settings.gradle \
  -x javadoc \
  -s
