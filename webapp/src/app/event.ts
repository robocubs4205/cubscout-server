import {Link} from "./link";
export class Event {
  id:number;
  shortName:string;
  address:string;

  startDate:Date;
  endDate:Date;

  links:Link[];

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
