import {Link} from "./link";
export class Game {
  id:number;
  name:string;
  type:string;
  year:number;

  links:Link[];

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
