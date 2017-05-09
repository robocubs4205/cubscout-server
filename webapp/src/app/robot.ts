import {Link} from "./link";
export class Robot {
  id:number;
  number:number;
  year:number;
  name:string;
  links:Link[];
  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
