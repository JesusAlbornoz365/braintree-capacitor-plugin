export interface BraintreeCapacitorPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
