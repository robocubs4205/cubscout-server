import {Link} from "./link";
import {RobotRole} from "./robot-role";
export class Scorecard {
  id:number;
  sections:ScorecardSection[];
  roles:RobotRole[];
  defaultRole:RobotRole;

  links:Link[];

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
export class ScorecardSection{
  id:number;
  sectionType:string;
  index:number;
  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
export class FieldSection extends ScorecardSection{
  label:string;
  type:string;
  nullWhen:NullWhen;
  checkboxMessage:string;
  optional:boolean;
  weight:number;
  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}
export class TitleSection extends ScorecardSection{
  title:string;
  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}
export class ParagraphSection extends ScorecardSection{
  text:string;
  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}
export enum NullWhen{
  CHECKED,
  UNCHECKED
}
