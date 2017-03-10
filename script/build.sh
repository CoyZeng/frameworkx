curr=$(cd "$(dirname "$0")" && pwd)
root=${curr:0:${#curr}-7}
gradle \
  cE e \
  -b ${root}/build.gradle \
  -p ${root}/ \
  --refresh-dependencies \
  $1
