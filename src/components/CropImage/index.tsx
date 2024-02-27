import {
  type ImageSourcePropType,
  type ImageProps,
  Text,
  Image,
} from 'react-native';
// @ts-ignore
import RCTCropImageView from './rnlib/Image.android';
import React, {memo} from 'react';

interface CropImageProps extends Omit<ImageProps, 'source'> {
  source: ImageSourcePropType;
  mask: ImageSourcePropType;
  replacePixelColor?: string;
}

const CropImage: React.ForwardRefRenderFunction<Image, CropImageProps> = (
  props,
  ref,
) => {
  return <RCTCropImageView {...props} />;
};

export default memo(CropImage);
