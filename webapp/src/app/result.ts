import {Link} from "./link";
import {FieldSection} from "./scorecard";
import {Robot} from "./robot";
export class Result {
  id:number;
  scores:ScorecardFieldResult[];
  links:Link[];
  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
export class ScorecardFieldResult{
  id:number;
  score:number;
  field:FieldSection;
  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
export class ResultSubmission extends Result{
  scorecard:number;
  robot:Robot;
  constructor(values: Object = {}) {
    super(values);
  }
}
