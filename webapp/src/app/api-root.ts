import {Link} from "./link";
export class ApiRoot {
  links:Link[];
  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
