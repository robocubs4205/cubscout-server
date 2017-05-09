import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {RatingComponent} from "./rating.component";
import {RobocubsPipesModule} from "../robocubs-pipes/robocubs-pipes.module";

@NgModule({
  imports: [
    CommonModule,
    RobocubsPipesModule
  ],
  declarations: [
    RatingComponent
  ],
  exports:[
    RatingComponent
  ]
})
export class RatingModule { }
