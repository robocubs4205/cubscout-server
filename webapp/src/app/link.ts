export class Link {
  rel:string;
  href:string;
  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
