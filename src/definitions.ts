export interface CallKitVoipPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
