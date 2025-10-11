declare module 'react-native/Libraries/Types/CodegenTypes' {
  import type { NativeSyntheticEvent } from 'react-native';
  
  export type BubblingEventHandler<T, PaperName extends string | never = never> = (
    event: NativeSyntheticEvent<T>
  ) => void | Promise<void>;
  
  export type DirectEventHandler<T, PaperName extends string | never = never> = (
    event: NativeSyntheticEvent<T>
  ) => void | Promise<void>;
}