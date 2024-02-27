import {
  type ImageSourcePropType,
  type ImageResolvedAssetSource,
  NativeModules,
  Image,
} from 'react-native';

const CropImageModule = NativeModules.CropImage;

export async function crop(
  originSource: ImageSourcePropType,
  maskSource: ImageSourcePropType,
  replacePixelColor?: string,
): Promise<ImageResolvedAssetSource> {
  const uri = await CropImageModule.crop(
    Image.resolveAssetSource(originSource),
    Image.resolveAssetSource(maskSource),
    replacePixelColor,
  );
  return Image.resolveAssetSource({uri});
}

export function clearTemp() {
  CropImageModule.clearTemp();
}

export default {
  crop,
  clearTemp,
};
