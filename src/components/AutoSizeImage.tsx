import {type ImageSourcePropType, type ImageProps, Image} from 'react-native';
import React, {useMemo} from 'react';
import useImgSize, {Size} from '../hooks/useImgSize';

interface AutoSizeImgCommon
  extends Omit<ImageProps, 'source' | 'width' | 'height'> {
  source: ImageSourcePropType;
  children?: (source: ImageSourcePropType, size: Size) => React.ReactNode;
}

interface AutoSizeImgByWidth extends AutoSizeImgCommon {
  width: number;
}

interface AutoSizeImgByHeight extends AutoSizeImgCommon {
  height: number;
}

type AutoSizeImgProps = AutoSizeImgByWidth | AutoSizeImgByHeight;

const AutoSizeImg: React.FC<AutoSizeImgProps> = props => {
  const {source, children, ...rest} = props;
  const {scaleByWidth, scaleByHeight} = useImgSize(source);
  const size = useMemo(() => {
    if ('width' in props) {
      return scaleByWidth(props.width);
    }
    return scaleByHeight(props.height);
  }, [
    source,
    (props as AutoSizeImgByWidth).width,
    (props as AutoSizeImgByHeight).height,
  ]);
  if (children) return children(source, size);
  return <Image {...rest} style={[rest.style, size]} source={source} />;
};

export default AutoSizeImg;
