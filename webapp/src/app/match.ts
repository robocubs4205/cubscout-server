import {Link} from "./link";
export class Match {
  id:number;
  number:number;
  type:string;
  links:Link[];
  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
