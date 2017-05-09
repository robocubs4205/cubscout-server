import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {HttpModule, JsonpModule} from "@angular/http";
import {RouterModule, Routes} from "@angular/router";
import {AppComponent} from "./app.component";
import {MaterialModule} from "@angular/material";
import "hammerjs";
import {ScorecardService} from "./scorecard.service";
import {GameService} from "./game.service";
import {EventService} from "./event.service";
import {ApiRootService} from "./api-root.service";
import {MakeScorecardComponent} from "./make-scorecard/make-scorecard.component";
import {ScoutComponent} from "./scout/scout.component";
import {MatchService} from "./match.service";
import {ResultService} from "./result.service";
import {ResultsModule} from "./results/results.module";
import {ResultsComponent} from "./results/results.component";
import {RobotService} from "./robot.service";
import {RatingModule} from "./rating/rating.module";
import {RobocubsPipesModule} from "./robocubs-pipes/robocubs-pipes.module";

const routs:Routes = [
  {path:'scout', component: ScoutComponent},
  {path:'',redirectTo:'scout',pathMatch:'full'},
  {path:'results',component:ResultsComponent}
];

@NgModule({
  declarations: [
    AppComponent,
    MakeScorecardComponent,
    ScoutComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    JsonpModule,
    MaterialModule,
    ResultsModule,
    RatingModule,
    RouterModule.forRoot(routs),
    RobocubsPipesModule
  ],
  providers: [
    ScorecardService,
    GameService,
    EventService,
    ApiRootService,
    MatchService,
    ResultService,
    RobotService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
