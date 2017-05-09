import {FieldSection} from "./scorecard";
export class RobotRole {
  id:number;
  weights:ScoreWeight[];
  name:string;
  description:string;
}
export class ScoreWeight{
  id:number;
  field:FieldSection;
  weight:number;
}
