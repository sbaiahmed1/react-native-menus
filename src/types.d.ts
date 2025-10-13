declare module 'react-native/Libraries/Types/CodegenTypes' {
  export type BubblingEventHandler<T> = (event: { nativeEvent: T }) => void;

  export type Float = number;
  export type Double = number;
}
